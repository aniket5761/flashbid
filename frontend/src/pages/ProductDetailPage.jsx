import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api, { getErrorMessage } from "../api/api";
import { ensureArray, formatCurrency, formatDateTime, normalizePage } from "../api/format";
import { createAuctionClient } from "../api/liveAuction";
import { useAuth } from "../state/AuthContext";
import EmptyState from "../component/EmptyState";
import Field from "../component/Field";
import PageHeader from "../component/PageHeader";
import StatusBadge from "../component/StatusBadge";

const initialPage = {
  content: [],
  number: 0,
  totalPages: 0,
  totalElements: 0
};

export default function ProductDetailPage() {
  const { productId } = useParams();
  const { isAuthenticated, user } = useAuth();
  const [product, setProduct] = useState(null);
  const [bids, setBids] = useState(initialPage);
  const [winner, setWinner] = useState(null);
  const [bidAmount, setBidAmount] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [liveStatus, setLiveStatus] = useState("connecting");

  const loadAuction = useCallback(async (options = {}) => {
    const { preserveMessages = false } = options;
    setLoading(true);
    if (!preserveMessages) {
      setError("");
      setSuccess("");
    }
    try {
      const productPromise = api.get(`/api/products/${productId}`);
      const bidsPromise = isAuthenticated
        ? api.get(`/api/bids/product/${productId}`, { params: { page: 0 } })
        : Promise.resolve({ data: initialPage });

      const [productRes, bidsRes] = await Promise.all([productPromise, bidsPromise]);
      const winnerRes = productRes.data?.productStatus === "CLOSED"
        ? await api.get(`/api/auctions/winner/${productId}`).catch(() => ({ data: null }))
        : { data: null };

      setProduct(productRes.data);
      setBids(normalizePage(bidsRes.data));
      setWinner(winnerRes.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      setBids(initialPage);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, productId]);

  useEffect(() => {
    loadAuction();
  }, [loadAuction]);

  useEffect(() => {
    if (!productId) {
      return undefined;
    }

    const client = createAuctionClient(productId, {
      onConnect: () => {
        setLiveStatus("connected");
        loadAuction({ preserveMessages: true });
      },
      onDisconnect: () => {
        setLiveStatus("disconnected");
      },
      onMessage: (event) => {
        if (event?.summary) {
          setProduct((current) => (
            current ? {
              ...current,
              productStatus: event.summary.status,
              currentBid: event.summary.currentBid,
              nextMinimumBid: event.summary.nextMinimumBid,
              minimumIncrement: event.summary.minimumIncrement,
              bidCount: event.summary.bidCount,
              startTime: event.summary.startTime ?? current.startTime,
              endTime: event.summary.endTime ?? current.endTime
            } : current
          ));
        }

        if (Array.isArray(event?.recentBids)) {
          setBids((current) => ({
            ...normalizePage(current),
            content: ensureArray(event.recentBids),
            totalElements: event.summary?.bidCount ?? event.recentBids.length
          }));
        }

        setWinner(event?.winner ?? null);
      },
      onError: (socketError) => {
        setLiveStatus("disconnected");
        console.error(socketError);
      }
    });

    return () => {
      setLiveStatus("disconnected");
      client.deactivate();
    };
  }, [loadAuction, productId]);

  async function handleBidSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    setSuccess("");

    try {
      await api.post("/api/bids", {
        productId: Number(productId),
        amount: Number(bidAmount)
      });
      setBidAmount("");
      setSuccess("Bid placed successfully.");
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div className="panel px-8 py-16 text-center text-base font-medium text-ink/60">Loading auction detail...</div>;
  }

  if (!product) {
    return <EmptyState title="Auction not found" description="The requested auction could not be loaded." />;
  }

  const canBid =
    isAuthenticated &&
    user?.role !== "ADMIN" &&
    product.productStatus === "OPEN" &&
    user?.id !== product.user?.id;
  const sellerLabel = product.user?.deleted ? "Deleted seller" : product.user?.username;
  const winnerLabel = winner?.user?.deleted ? "Deleted account" : winner?.user?.username;
  const statusTone =
    product.productStatus === "OPEN" ? "teal" : product.productStatus === "SCHEDULED" ? "amber" : "slate";
  const bidPage = normalizePage(bids);

  return (
    <div className="space-y-8">
      <PageHeader
        eyebrow="Auction detail"
        title={product.name}
        description={product.description || "No description was provided for this auction."}
        actions={
          <Link to="/products" className="button-secondary">
            Back to marketplace
          </Link>
        }
      />

      {error ? <div className="rounded-3xl bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">{error}</div> : null}
      {success ? <div className="rounded-3xl bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{success}</div> : null}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap gap-2">
          <StatusBadge tone={statusTone}>{product.productStatus}</StatusBadge>
          <StatusBadge tone={liveStatus === "connected" ? "teal" : liveStatus === "connecting" ? "amber" : "rose"}>
            Live {liveStatus}
          </StatusBadge>
        </div>
        <div className="rounded-full border border-ink/10 bg-white/80 px-4 py-2 text-sm text-ink/65">
          Seller <span className="font-semibold text-ink">{sellerLabel || "Unknown"}</span>
        </div>
      </div>

      <section className="grid gap-6 lg:grid-cols-[1.08fr_0.92fr]">
        <div className="panel px-6 py-6">
          <div className="mb-6 rounded-[28px] bg-linear-to-br from-ink via-slateblue to-teal px-6 py-6 text-white">
            <p className="text-xs font-semibold uppercase tracking-[0.22em] text-white/60">Auction position</p>
            <div className="mt-4 grid gap-4 sm:grid-cols-3">
              <MetricItem label="Current bid" value={formatCurrency(product.currentBid)} />
              <MetricItem label="Next minimum" value={formatCurrency(product.nextMinimumBid)} />
              <MetricItem label="Bid count" value={product.bidCount ?? 0} />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <InfoCard label="Base price" value={formatCurrency(product.startingPrice)} />
            <InfoCard label="Minimum increment" value={formatCurrency(product.minimumIncrement)} />
            <InfoCard label="Starts" value={formatDateTime(product.startTime)} />
            <InfoCard label="Ends" value={formatDateTime(product.endTime)} />
            <InfoCard label="Status" value={product.productStatus} />
            <InfoCard label="Seller" value={sellerLabel} />
          </div>

          {winner ? (
            <div className="mt-6 rounded-3xl border border-emerald-200 bg-emerald-50 px-5 py-5">
              <p className="text-xs font-semibold uppercase tracking-[0.22em] text-emerald-700">Winning bidder</p>
              <h2 className="mt-3 font-display text-3xl font-bold text-ink">{winnerLabel}</h2>
              <p className="mt-2 text-sm text-ink/65">Winning amount: {formatCurrency(winner.amount)}</p>
            </div>
          ) : null}
        </div>

        <div className="space-y-6">
          <div className="panel px-6 py-6">
            <h2 className="font-display text-3xl font-bold text-ink">Place a bid</h2>
            {!isAuthenticated ? (
              <p className="mt-4 text-sm leading-7 text-ink/65">
                <Link to="/login" className="font-semibold underline underline-offset-4">
                  Log in
                </Link>{" "}
                to bid and view bid history.
              </p>
            ) : canBid ? (
              <form onSubmit={handleBidSubmit} className="mt-5 space-y-4">
                <Field
                  label="Bid amount"
                  type="number"
                  min={product.nextMinimumBid}
                  value={bidAmount}
                  onChange={(event) => setBidAmount(event.target.value)}
                  placeholder={`${product.nextMinimumBid}`}
                />
                <button type="submit" className="button-primary w-full" disabled={submitting}>
                  {submitting ? "Submitting..." : "Place bid"}
                </button>
              </form>
            ) : (
              <p className="mt-4 text-sm leading-7 text-ink/65">
                {user?.id === product.user?.id
                  ? "You cannot bid on your own auction."
                  : user?.role === "ADMIN"
                    ? "Admin accounts monitor auctions but do not bid from this UI."
                    : `Bidding is unavailable because this auction is ${product.productStatus.toLowerCase()}.`}
              </p>
            )}
          </div>

          <div className="panel px-6 py-6">
            <div className="flex items-center justify-between gap-3">
              <h2 className="font-display text-3xl font-bold text-ink">Bid history</h2>
              <span className="text-sm text-ink/55">{bidPage.totalElements || 0} bids</span>
            </div>
            {isAuthenticated ? (
              <div className="mt-5 space-y-3">
                {bidPage.content.length ? bidPage.content.map((bid) => (
                  <div key={bid.id} className="rounded-2xl border border-ink/10 bg-white px-4 py-4">
                    <div className="flex items-center justify-between gap-3">
                      <p className="font-semibold text-ink">{bid.bidderUsername}</p>
                      <p className="font-semibold text-ink">{formatCurrency(bid.amount)}</p>
                    </div>
                    <p className="mt-1 text-sm text-ink/55">{formatDateTime(bid.timestamp)}</p>
                  </div>
                )) : <p className="text-sm text-ink/60">No bids have been placed yet.</p>}
              </div>
            ) : (
              <p className="mt-4 text-sm text-ink/60">Sign in to view bidder activity.</p>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}

function InfoCard({ label, value }) {
  return (
    <div className="rounded-3xl border border-ink/10 bg-white px-4 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.22em] text-ink/40">{label}</p>
      <p className="mt-2 text-sm font-semibold text-ink">{value || "Unavailable"}</p>
    </div>
  );
}

function MetricItem({ label, value }) {
  return (
    <div className="rounded-[22px] border border-white/10 bg-white/6 px-4 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.2em] text-white/55">{label}</p>
      <p className="mt-3 font-display text-2xl font-bold text-white">{value}</p>
    </div>
  );
}
