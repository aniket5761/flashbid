import { useEffect, useState } from "react";
import api, { getErrorMessage } from "../api/api";
import { formatCurrency, normalizePage } from "../api/format";
import EmptyState from "../component/EmptyState";
import PageHeader from "../component/PageHeader";
import StatCard from "../component/StatCard";
import StatusBadge from "../component/StatusBadge";

export default function AdminDashboardPage() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadDashboard() {
    setLoading(true);
    setError("");
    try {
      const [usersRes, pendingRes, bannedRes, openRes, scheduledRes, closedRes] = await Promise.all([
        api.get("/api/user/all", { params: { page: 0 } }),
        api.get("/api/user/all", { params: { page: 0, sellerRequested: true } }),
        api.get("/api/user/all", { params: { page: 0, banned: true } }),
        api.get("/api/products", { params: { page: 0, productStatus: "OPEN" } }),
        api.get("/api/products", { params: { page: 0, productStatus: "SCHEDULED" } }),
        api.get("/api/products", { params: { page: 0, productStatus: "CLOSED" } })
      ]);

      setDashboard({
        users: normalizePage(usersRes.data),
        pendingUsers: normalizePage(pendingRes.data),
        bannedUsers: normalizePage(bannedRes.data),
        openAuctions: normalizePage(openRes.data),
        scheduledAuctions: normalizePage(scheduledRes.data),
        closedAuctions: normalizePage(closedRes.data)
      });
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      setDashboard(null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  async function handleSellerApproval(userId, value) {
    try {
      await api.patch(`/api/user/${userId}/seller-approval`, { value });
      await loadDashboard();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  }

  async function handleBan(userId, value) {
    try {
      await api.patch(`/api/user/${userId}/ban`, { value });
      await loadDashboard();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  }

  async function handleForceClose(productId) {
    try {
      await api.post(`/api/auctions/${productId}/close`);
      await loadDashboard();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  }

  if (loading) {
    return <div className="panel px-8 py-16 text-center text-base font-medium text-ink/60">Loading admin dashboard...</div>;
  }

  return (
    <div className="space-y-8">
      <PageHeader
        eyebrow="Admin dashboard"
        title="Moderate users and auctions"
        description="Approve sellers, ban or restore users, and force-close auctions while monitoring marketplace activity."
      />

      <div className="flex flex-wrap gap-2">
        <StatusBadge tone="coral">Admin controls</StatusBadge>
        <StatusBadge tone="amber">Seller review queue</StatusBadge>
        <StatusBadge tone="teal">Live auction oversight</StatusBadge>
      </div>

      {error ? <div className="rounded-3xl bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">{error}</div> : null}

      {dashboard ? (
        <>
          <section className="grid gap-5 md:grid-cols-3">
            <StatCard label="Users" value={dashboard.users.totalElements} tone="ink" />
            <StatCard label="Pending sellers" value={dashboard.pendingUsers.totalElements} tone="coral" />
            <StatCard label="Banned users" value={dashboard.bannedUsers.totalElements} tone="teal" />
          </section>

          <section className="grid gap-6 lg:grid-cols-2">
            <div className="panel px-6 py-6">
              <h2 className="font-display text-3xl font-bold text-ink">Seller approvals</h2>
              <div className="mt-5 space-y-3">
                {dashboard.pendingUsers.content.length ? dashboard.pendingUsers.content.map((pendingUser) => (
                  <div key={pendingUser.id} className="rounded-2xl border border-ink/10 bg-white px-4 py-4">
                    <p className="font-semibold text-ink">{pendingUser.username}</p>
                    <p className="mt-1 text-sm text-ink/55">{pendingUser.email}</p>
                    <div className="mt-4 flex flex-wrap gap-3">
                      <button type="button" className="button-primary" onClick={() => handleSellerApproval(pendingUser.id, true)}>
                        Approve seller
                      </button>
                      <button type="button" className="button-secondary" onClick={() => handleSellerApproval(pendingUser.id, false)}>
                        Reject request
                      </button>
                    </div>
                  </div>
                )) : <p className="text-sm text-ink/60">No pending seller requests.</p>}
              </div>
            </div>

            <div className="panel px-6 py-6">
              <h2 className="font-display text-3xl font-bold text-ink">Auction overview</h2>
              <div className="mt-5 grid gap-4 sm:grid-cols-3">
                <CountCard label="Open" value={dashboard.openAuctions.totalElements} />
                <CountCard label="Scheduled" value={dashboard.scheduledAuctions.totalElements} />
                <CountCard label="Closed" value={dashboard.closedAuctions.totalElements} />
              </div>
            </div>
          </section>

          <section className="grid gap-6 lg:grid-cols-[1.05fr_0.95fr]">
            <div className="panel px-6 py-6">
              <h2 className="font-display text-3xl font-bold text-ink">User moderation</h2>
              <div className="mt-5 space-y-3">
                {dashboard.users.content.length ? dashboard.users.content.map((account) => (
                  <div key={account.id} className="rounded-2xl border border-ink/10 bg-white px-4 py-4">
                    <div className="flex items-center justify-between gap-3">
                      <div>
                        <p className="font-semibold text-ink">{account.username}</p>
                        <p className="mt-1 text-sm text-ink/55">{account.email}</p>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        <StatusBadge tone={account.role === "ADMIN" ? "coral" : account.role === "SELLER" ? "teal" : "ink"}>
                          {account.role}
                        </StatusBadge>
                        {account.banned ? <StatusBadge tone="rose">Banned</StatusBadge> : null}
                      </div>
                    </div>
                    <div className="mt-4 flex flex-wrap gap-3">
                      <button type="button" className="button-secondary" onClick={() => handleBan(account.id, !account.banned)}>
                        {account.banned ? "Unban user" : "Ban user"}
                      </button>
                    </div>
                  </div>
                )) : <p className="text-sm text-ink/60">No users available.</p>}
              </div>
            </div>

            <div className="panel px-6 py-6">
              <h2 className="font-display text-3xl font-bold text-ink">Manual auction close</h2>
              <div className="mt-5 space-y-3">
                {dashboard.openAuctions.content.length ? dashboard.openAuctions.content.map((auction) => (
                  <div key={auction.id} className="rounded-2xl border border-ink/10 bg-white px-4 py-4">
                    <div className="flex items-center justify-between gap-3">
                      <div>
                        <p className="font-semibold text-ink">{auction.name}</p>
                        <p className="mt-1 text-sm text-ink/55">Current bid: {formatCurrency(auction.currentBid)}</p>
                      </div>
                      <button type="button" className="button-primary" onClick={() => handleForceClose(auction.id)}>
                        Force close
                      </button>
                    </div>
                  </div>
                )) : <EmptyState title="No open auctions" description="There are currently no active auctions to close." />}
              </div>
            </div>
          </section>
        </>
      ) : null}
    </div>
  );
}

function CountCard({ label, value }) {
  return (
    <div className="rounded-[24px] border border-ink/10 bg-white px-4 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.22em] text-ink/40">{label}</p>
      <p className="mt-3 font-display text-3xl font-bold text-ink">{value}</p>
    </div>
  );
}
