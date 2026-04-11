import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { getErrorMessage } from "../api/api";
import { useAuth } from "../state/AuthContext";
import EmptyState from "../component/EmptyState";
import PageHeader from "../component/PageHeader";
import ProductCard from "../component/ProductCard";
import StatCard from "../component/StatCard";
import StatusBadge from "../component/StatusBadge";
import { ensureArray, formatCurrency, normalizePage } from "../api/format";

export default function Home() {
  const { isAuthenticated, user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(isAuthenticated);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isAuthenticated || !user) {
      setDashboard(null);
      setLoading(false);
      return;
    }

    async function loadDashboard() {
      setLoading(true);
      setError("");
      try {
        if (user.role === "ADMIN") {
          const [usersRes, pendingRes, openRes, scheduledRes] = await Promise.all([
            api.get("/api/user/all", { params: { page: 0 } }),
            api.get("/api/user/all", { params: { page: 0, sellerRequested: true } }),
            api.get("/api/products", { params: { page: 0, productStatus: "OPEN" } }),
            api.get("/api/products", { params: { page: 0, productStatus: "SCHEDULED" } })
          ]);

          setDashboard({
            role: "ADMIN",
            stats: [
              { label: "Users", value: normalizePage(usersRes.data).totalElements, tone: "ink" },
              { label: "Seller requests", value: normalizePage(pendingRes.data).totalElements, tone: "coral" },
              { label: "Open auctions", value: normalizePage(openRes.data).totalElements, tone: "teal" }
            ],
            pendingUsers: normalizePage(pendingRes.data).content,
            openAuctions: normalizePage(openRes.data).content,
            scheduledCount: normalizePage(scheduledRes.data).totalElements
          });
          return;
        }

        if (user.role === "SELLER") {
          const [productsRes, bidsRes] = await Promise.all([
            api.get(`/api/products/user/${user.id}`),
            api.get(`/api/bids/user/${user.id}`, { params: { page: 0 } })
          ]);

          const ownProducts = ensureArray(productsRes.data);
          setDashboard({
            role: "SELLER",
            stats: [
              { label: "My auctions", value: ownProducts.length, tone: "ink" },
              { label: "Open now", value: ownProducts.filter((product) => product.productStatus === "OPEN").length, tone: "teal" },
              { label: "My bids", value: normalizePage(bidsRes.data).totalElements, tone: "coral" }
            ],
            products: ownProducts
          });
          return;
        }

        const [openRes, bidsRes] = await Promise.all([
          api.get("/api/products", { params: { page: 0, productStatus: "OPEN" } }),
          api.get(`/api/bids/user/${user.id}`, { params: { page: 0 } })
        ]);

        setDashboard({
          role: "USER",
          stats: [
            { label: "Open auctions", value: normalizePage(openRes.data).totalElements, tone: "teal" },
            { label: "My bids", value: normalizePage(bidsRes.data).totalElements, tone: "coral" },
            { label: "Seller request", value: user.sellerRequested ? "Pending" : "Inactive", tone: "ink" }
          ],
          openAuctions: normalizePage(openRes.data).content,
          bids: normalizePage(bidsRes.data).content
        });
      } catch (requestError) {
        setError(getErrorMessage(requestError));
        setDashboard(null);
      } finally {
        setLoading(false);
      }
    }

    loadDashboard();
  }, [isAuthenticated, user]);

  if (!isAuthenticated) {
    return (
      <div className="space-y-8">
        <PageHeader
          eyebrow="Marketplace"
          title="Bid live, launch listings, and run marketplace ops from one place"
          description="FlashBid brings real-time bidding, seller tools, and admin moderation into one focused workspace instead of scattering them across demo-like screens."
          actions={
            <>
              <Link to="/products" className="button-primary">
                View marketplace
              </Link>
              <Link to="/register" className="button-secondary">
                Create account
              </Link>
            </>
          }
        />

        <section className="stats-grid">
          <StatCard label="Bidder tools" value="Live bids" tone="teal" />
          <StatCard label="Seller tools" value="Timed auctions" tone="coral" />
          <StatCard label="Admin tools" value="Moderation" tone="ink" />
        </section>

        <section className="feature-grid">
          <div className="feature-card feature-card--primary panel">
            <div className="eyebrow">Why FlashBid</div>
            <h2 className="mt-5 font-display text-3xl font-bold text-ink">A cleaner auction flow for bidders, sellers, and admins.</h2>
            <p className="mt-4 max-w-2xl text-base leading-7 text-ink/68">
              Browse as a guest, then sign in when you are ready to place bids, request seller access, or manage the marketplace.
            </p>
          </div>
          <div className="feature-grid__stack">
            <div className="feature-card panel">
              <p className="text-xs font-semibold uppercase tracking-[0.24em] text-ink/40">Real-time</p>
              <p className="mt-3 text-sm leading-7 text-ink/65">Live auction events keep bid values and statuses fresh without constant refreshes.</p>
            </div>
            <div className="feature-card panel">
              <p className="text-xs font-semibold uppercase tracking-[0.24em] text-ink/40">Role-aware</p>
              <p className="mt-3 text-sm leading-7 text-ink/65">The workspace changes with your account, from bidder tracking to seller tools and admin moderation.</p>
            </div>
          </div>
        </section>
      </div>
    );
  }

  const dashboardStats = dashboard?.stats ?? [];
  const pendingUsers = dashboard?.pendingUsers ?? [];
  const openAuctions = dashboard?.openAuctions ?? [];
  const sellerProducts = dashboard?.products ?? [];
  const recentBids = dashboard?.bids ?? [];

  return (
    <div className="space-y-8">
      <PageHeader
        eyebrow="Overview"
        title={user.role === "ADMIN" ? "Admin dashboard" : user.role === "SELLER" ? "Seller studio" : "My marketplace"}
        description={
          user.role === "ADMIN"
            ? "Monitor seller approvals, users, and auction activity from a single place."
            : user.role === "SELLER"
              ? "Review your listings, inspect bidding activity, and publish new auctions."
              : "Track open auctions and the bids you have already placed."
        }
        actions={
          user.role === "ADMIN" ? (
            <Link to="/admin" className="button-primary">
              Open admin dashboard
            </Link>
          ) : user.role === "SELLER" ? (
            <>
              <Link to="/seller" className="button-secondary">
                Seller studio
              </Link>
              <Link to="/products/new" className="button-primary">
                Create auction
              </Link>
            </>
          ) : (
            <>
              <Link to="/products" className="button-secondary">
                Browse auctions
              </Link>
              <Link to="/seller" className="button-primary">
                Seller studio
              </Link>
            </>
          )
        }
      />

      <section className="hero-grid">
        <div className="hero-card panel">
          <div className="hero-card__glow" />
          <div className="relative flex flex-wrap items-center gap-3">
            <StatusBadge tone={user.role === "ADMIN" ? "coral" : user.role === "SELLER" ? "teal" : "ink"}>
              {user.role} workspace
            </StatusBadge>
            {user.sellerRequested && user.role === "USER" ? <StatusBadge tone="amber">Approval pending</StatusBadge> : null}
          </div>
          <h2 className="hero-card__title">
            {user.role === "ADMIN"
              ? "See approvals, auction counts, and moderation actions at a glance."
              : user.role === "SELLER"
                ? "Keep your active listings, incoming bids, and next auction move in one view."
                : "Track your current bidding position and jump back into live auctions faster."}
          </h2>
        </div>
        <div className="hero-action">
          <p className="text-xs font-semibold uppercase tracking-[0.22em] text-white/60">Quick action</p>
          <p className="mt-3 text-sm leading-7 text-white/75">
            {user.role === "ADMIN"
              ? "Review the moderation queue before new seller requests pile up."
              : user.role === "SELLER"
                ? "Publish the next listing while your current auctions are still in motion."
                : "Browse open auctions and place your next bid before the timer closes."}
          </p>
          <div className="mt-5">
            <Link to={user.role === "ADMIN" ? "/admin" : user.role === "SELLER" ? "/products/new" : "/products"} className="button-secondary border-white/15! bg-white/10! text-white! hover:bg-white/16!">
              {user.role === "ADMIN" ? "Open admin tools" : user.role === "SELLER" ? "Create auction" : "Browse live auctions"}
            </Link>
          </div>
        </div>
      </section>

      {error ? <div className="rounded-3xl bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">{error}</div> : null}

      {loading ? (
        <div className="panel px-8 py-16 text-center text-base font-medium text-ink/60">Loading your overview...</div>
      ) : (
        <>
          <section className="stats-grid">
            {dashboardStats.map((stat) => (
              <StatCard key={stat.label} label={stat.label} value={stat.value} tone={stat.tone} />
            ))}
          </section>

          {dashboard?.role === "ADMIN" ? (
            <section className="content-grid-two">
              <div className="panel px-6 py-6">
                <h2 className="font-display text-3xl font-bold text-ink">Pending seller approvals</h2>
                <div className="mt-5 space-y-3">
                  {pendingUsers.length ? pendingUsers.map((pendingUser) => (
                    <div key={pendingUser.id} className="rounded-2xl border border-ink/10 bg-white px-4 py-4">
                      <div className="flex items-center justify-between gap-3">
                        <p className="font-semibold text-ink">{pendingUser.username}</p>
                        <StatusBadge tone="amber">Pending</StatusBadge>
                      </div>
                      <p className="mt-1 text-sm text-ink/55">{pendingUser.email}</p>
                    </div>
                  )) : <p className="text-sm text-ink/60">No seller approvals waiting right now.</p>}
                </div>
              </div>

              <div className="panel px-6 py-6">
                <h2 className="font-display text-3xl font-bold text-ink">Auction status</h2>
                <div className="mini-stats-grid">
                  <OverviewInfo label="Open auctions" value={openAuctions.length} />
                  <OverviewInfo label="Scheduled auctions" value={dashboard.scheduledCount} />
                </div>
              </div>
            </section>
          ) : null}

          {dashboard?.role === "SELLER" ? (
            sellerProducts.length ? (
              <section className="content-grid-two">
                {sellerProducts.slice(0, 4).map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </section>
            ) : (
              <EmptyState title="No auctions yet" description="Use Create Auction to publish your first listing." />
            )
          ) : null}

          {dashboard?.role === "USER" ? (
            <section className="content-grid-main">
              <div>
                <h2 className="mb-4 font-display text-3xl font-bold text-ink">Open auctions</h2>
                {openAuctions.length ? (
                  <div className="grid gap-6">
                    {openAuctions.slice(0, 3).map((product) => (
                      <ProductCard key={product.id} product={product} />
                    ))}
                  </div>
                ) : (
                  <EmptyState title="No open auctions" description="There are no active bidding windows right now." />
                )}
              </div>

              <div className="panel px-6 py-6">
                <h2 className="font-display text-3xl font-bold text-ink">Recent bids</h2>
                <div className="mt-5 space-y-3">
                  {recentBids.length ? recentBids.map((bid) => (
                    <div key={bid.id} className="rounded-2xl border border-ink/10 bg-white px-4 py-4">
                      <p className="font-semibold text-ink">Product #{bid.productId}</p>
                      <p className="mt-1 text-sm text-ink/55">Bid amount: {formatCurrency(bid.amount)}</p>
                    </div>
                  )) : <p className="text-sm text-ink/60">You have not placed any bids yet.</p>}
                </div>
              </div>
            </section>
          ) : null}
        </>
      )}
    </div>
  );
}

function OverviewInfo({ label, value }) {
  return (
    <div className="rounded-3xl border border-ink/10 bg-white px-4 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.22em] text-ink/40">{label}</p>
      <p className="mt-3 font-display text-3xl font-bold text-ink">{value}</p>
    </div>
  );
}
