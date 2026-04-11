import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { getErrorMessage } from "../api/api";
import { ensureArray } from "../api/format";
import { useAuth } from "../state/AuthContext";
import EmptyState from "../component/EmptyState";
import PageHeader from "../component/PageHeader";
import ProductCard from "../component/ProductCard";
import StatusBadge from "../component/StatusBadge";

export default function SellerStudio() {
  const { user, updateUser } = useAuth();
  const [profile, setProfile] = useState(user);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const canSell = profile?.role === "SELLER" || profile?.role === "ADMIN";

  const loadStudio = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data: me } = await api.get("/api/user/me");
      setProfile(me);
      updateUser(me);

      if (me.role === "SELLER" || me.role === "ADMIN") {
        const { data: ownProducts } = await api.get(`/api/products/user/${me.id}`);
        setProducts(ensureArray(ownProducts));
      } else {
        setProducts([]);
      }
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      setProducts([]);
    } finally {
      setLoading(false);
    }
  }, [updateUser]);

  useEffect(() => {
    loadStudio();
  }, [loadStudio]);

  async function handleSellerRequest() {
    setWorking(true);
    setError("");
    setSuccess("");
    try {
      const { data } = await api.post("/api/user/me/seller-request");
      setProfile(data);
      updateUser(data);
      setSuccess("Seller request submitted. An admin can review it from the dashboard.");
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setWorking(false);
    }
  }

  if (loading) {
    return <div className="panel px-8 py-16 text-center text-base font-medium text-ink/60">Loading seller studio...</div>;
  }

  return (
    <div className="space-y-8">
      <PageHeader
        eyebrow="Seller studio"
        title={canSell ? "Manage your listings" : "Request seller access"}
        description={
          canSell
            ? "Publish auctions, review your products, and open each auction to inspect bids."
            : "Request approval to become a seller and unlock auction creation."
        }
        actions={
          canSell ? (
            <Link to="/products/new" className="button-primary">
              Create auction
            </Link>
          ) : null
        }
      />

      <div className="flex flex-wrap gap-2">
        <StatusBadge tone={canSell ? "teal" : "ink"}>{canSell ? profile?.role : "Bidder account"}</StatusBadge>
        {profile?.sellerRequested && !canSell ? <StatusBadge tone="amber">Approval pending</StatusBadge> : null}
      </div>

      {error ? <div className="rounded-3xl bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">{error}</div> : null}
      {success ? <div className="rounded-3xl bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{success}</div> : null}

      {!canSell ? (
        <div className="panel px-8 py-8">
          <h2 className="font-display text-3xl font-bold text-ink">
            {profile?.sellerRequested ? "Seller request pending" : "Become a seller"}
          </h2>
          <p className="mt-4 max-w-2xl text-base leading-7 text-ink/65">
            {profile?.sellerRequested
              ? "Your seller request is waiting for admin approval. Once approved, seller tools unlock automatically."
              : "Seller accounts can create auctions, set minimum increments, and monitor bidder activity."}
          </p>
          {!profile?.sellerRequested ? (
            <button type="button" onClick={handleSellerRequest} className="button-primary mt-6" disabled={working}>
              {working ? "Submitting..." : "Request seller access"}
            </button>
          ) : null}
        </div>
      ) : products.length ? (
        <section className="grid gap-6 lg:grid-cols-2">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </section>
      ) : (
        <EmptyState title="No auctions yet" description="Create your first listing to start receiving bids." />
      )}
    </div>
  );
}
