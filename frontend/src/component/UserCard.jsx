import { formatDateTime } from "../api/format";
import StatusBadge from "./StatusBadge";

export default function UserCard({ user }) {
  return (
    <article className="panel h-full px-6 py-6 transition duration-200 hover:-translate-y-1 hover:shadow-2xl hover:shadow-ink/5">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-ink/40">User #{user.id}</p>
          <h3 className="mt-3 font-display text-2xl font-bold text-ink">
            {user.firstName || user.lastName
              ? `${user.firstName || ""} ${user.lastName || ""}`.trim()
              : user.username}
          </h3>
          <p className="mt-1 text-sm font-medium text-ink/55">@{user.username}</p>
        </div>
        <StatusBadge tone={user.role === "ADMIN" ? "coral" : user.role === "SELLER" ? "teal" : "ink"}>
          {user.role}
        </StatusBadge>
      </div>

      <div className="mt-5 flex flex-wrap gap-2">
        {user.sellerRequested ? <StatusBadge tone="amber">Seller request</StatusBadge> : null}
        {user.banned ? <StatusBadge tone="rose">Banned</StatusBadge> : null}
        {user.deleted ? <StatusBadge tone="slate">Deleted</StatusBadge> : null}
      </div>

      <div className="mt-6 grid gap-3">
        <InfoItem label="Email" value={user.email} />
        <InfoItem label="Joined" value={formatDateTime(user.registrationDate)} />
      </div>
    </article>
  );
}

function InfoItem({ label, value }) {
  return (
    <div className="rounded-2xl border border-ink/10 bg-white px-4 py-3">
      <p className="text-xs font-semibold uppercase tracking-[0.22em] text-ink/40">{label}</p>
      <p className="mt-2 break-all text-sm font-semibold text-ink">{value || "Unavailable"}</p>
    </div>
  );
}
