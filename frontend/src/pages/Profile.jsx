import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { getErrorMessage } from "../api/api";
import { ensureArray, formatCurrency, formatDateTime, normalizePage } from "../api/format";
import { useAuth } from "../state/AuthContext";
import ConfirmDialog from "../component/ConfirmDialog";
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

export default function Profile() {
  const navigate = useNavigate();
  const { updateUser, logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ firstName: "", lastName: "", email: "" });
  const [bids, setBids] = useState(initialPage);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [confirmAction, setConfirmAction] = useState(null);

  useEffect(() => {
    async function loadProfile() {
      setLoading(true);
      setError("");
      try {
        const { data: me } = await api.get("/api/user/me");
        setProfile(me);
        setForm({
          firstName: me.firstName || "",
          lastName: me.lastName || "",
          email: me.email || ""
        });
        updateUser(me);

        const [bidsRes, productsRes] = await Promise.all([
          api.get(`/api/bids/user/${me.id}`, { params: { page: 0 } }),
          api.get(`/api/products/user/${me.id}`)
        ]);

        setBids(normalizePage(bidsRes.data));
        setProducts(ensureArray(productsRes.data));
      } catch (requestError) {
        setError(getErrorMessage(requestError));
        setBids(initialPage);
        setProducts([]);
      } finally {
        setLoading(false);
      }
    }

    loadProfile();
  }, [updateUser]);

  async function submitProfileUpdate() {
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      const { data } = await api.put(`/api/user/${profile.id}`, form);
      setProfile(data);
      updateUser(data);
      setSuccess("Profile updated successfully.");
      setConfirmAction(null);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  function handleSubmit(event) {
    event.preventDefault();
    if (!profile) {
      return;
    }

    setError("");
    setSuccess("");
    setConfirmAction({
      title: "Save profile changes?",
      description: "Your account details will be updated with the information currently shown in the form.",
      confirmLabel: "Yes, update profile",
      tone: "default",
      onConfirm: submitProfileUpdate
    });
  }

  async function handleDelete() {
    if (!profile) {
      return;
    }

    try {
      await api.delete(`/api/user/${profile.id}`);
      logout();
      navigate("/");
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  }

  if (loading) {
    return <div className="panel px-8 py-16 text-center text-base font-medium text-ink/60">Loading your profile...</div>;
  }

  if (!profile) {
    return <EmptyState title="Profile unavailable" description="Please sign in again to continue." />;
  }

  return (
    <div className="space-y-8">
      <ConfirmDialog
        open={Boolean(confirmAction)}
        title={confirmAction?.title}
        description={confirmAction?.description}
        confirmLabel={confirmAction?.confirmLabel}
        tone={confirmAction?.tone}
        busy={saving}
        onConfirm={confirmAction?.onConfirm}
        onCancel={() => !saving && setConfirmAction(null)}
      />

      <PageHeader
        eyebrow="My profile"
        title={profile.firstName ? `${profile.firstName} ${profile.lastName}` : profile.username}
        description="Update your personal details, review your bids, inspect your auctions, and delete your account if needed."
      />

      <div className="flex flex-wrap gap-2">
        <StatusBadge tone={profile.role === "ADMIN" ? "coral" : profile.role === "SELLER" ? "teal" : "ink"}>
          {profile.role}
        </StatusBadge>
        {profile.sellerRequested ? <StatusBadge tone="amber">Seller request pending</StatusBadge> : null}
      </div>

      {error ? <div className="rounded-3xl bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">{error}</div> : null}
      {success ? <div className="rounded-3xl bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{success}</div> : null}

      <section className="grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
        <form onSubmit={handleSubmit} className="panel grid gap-5 px-8 py-8 md:grid-cols-2">
          <Field label="First name" value={form.firstName} onChange={(event) => setForm((current) => ({ ...current, firstName: event.target.value }))} />
          <Field label="Last name" value={form.lastName} onChange={(event) => setForm((current) => ({ ...current, lastName: event.target.value }))} />
          <div className="md:col-span-2">
            <Field label="Email" type="email" value={form.email} onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))} />
          </div>
          <div className="md:col-span-2 rounded-3xl border border-ink/10 bg-sand/55 px-4 py-4 text-sm leading-7 text-ink/65">
            Changes stay in the form until you confirm them. Selecting <span className="font-semibold text-ink">Save profile</span> opens a review step before anything is updated.
          </div>
          <div className="md:col-span-2 flex flex-wrap justify-between gap-3">
            <button
              type="button"
              onClick={() => setConfirmAction({
                title: "Delete this account?",
                description: "This action is permanent. Your account will be removed and you will be signed out immediately.",
                confirmLabel: "Delete account",
                tone: "danger",
                onConfirm: handleDelete
              })}
              className="button-secondary border-rose-200 text-rose-700 hover:border-rose-300"
            >
              Delete profile
            </button>
            <button type="submit" className="button-primary min-w-40" disabled={saving}>
              {saving ? "Saving..." : "Save profile"}
            </button>
          </div>
        </form>

        <div className="panel px-8 py-8">
          <h2 className="font-display text-3xl font-bold text-ink">Account status</h2>
          <div className="mt-5 grid gap-4">
            <InfoCard label="Username" value={profile.username} />
            <InfoCard label="Role" value={profile.role} />
            <InfoCard label="Seller request" value={profile.sellerRequested ? "Pending" : profile.role === "SELLER" ? "Approved" : "Not requested"} />
            <InfoCard label="Joined" value={formatDateTime(profile.registrationDate)} />
          </div>
        </div>
      </section>

      <section className="grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
        <div className="panel px-6 py-6">
          <div className="flex items-center justify-between gap-3">
            <h2 className="font-display text-3xl font-bold text-ink">My bid history</h2>
            <span className="text-sm text-ink/55">{bids.totalElements} bids</span>
          </div>
          <div className="mt-5 space-y-3">
            {bids.content.length ? bids.content.map((bid) => (
              <div key={bid.id} className="rounded-2xl border border-ink/10 bg-white px-4 py-4">
                <div className="flex items-center justify-between gap-3">
                  <p className="font-semibold text-ink">Product #{bid.productId}</p>
                  <p className="font-semibold text-ink">{formatCurrency(bid.amount)}</p>
                </div>
                <p className="mt-1 text-sm text-ink/55">{formatDateTime(bid.timestamp)}</p>
              </div>
            )) : <p className="text-sm text-ink/60">You have not placed any bids yet.</p>}
          </div>
        </div>

        <div className="panel px-6 py-6">
          <div className="flex items-center justify-between gap-3">
            <h2 className="font-display text-3xl font-bold text-ink">My auctions</h2>
            <span className="text-sm text-ink/55">{products.length} listings</span>
          </div>
          <div className="mt-5 space-y-3">
            {products.length ? products.map((product) => (
              <div key={product.id} className="rounded-2xl border border-ink/10 bg-white px-4 py-4">
                <div className="flex items-center justify-between gap-3">
                  <p className="font-semibold text-ink">{product.name}</p>
                  <p className="font-semibold text-ink">{product.productStatus}</p>
                </div>
                <p className="mt-1 text-sm text-ink/55">Current bid: {formatCurrency(product.currentBid)}</p>
              </div>
            )) : <p className="text-sm text-ink/60">You have not created any auctions yet.</p>}
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
