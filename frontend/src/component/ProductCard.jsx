import { Link } from "react-router-dom";
import { formatCurrency, formatDateTime } from "../api/format";
import StatusBadge from "./StatusBadge";

const statusTones = {
  SCHEDULED: "amber",
  OPEN: "teal",
  CLOSED: "slate"
};

export default function ProductCard({ product }) {
  const sellerLabel = product.user?.deleted ? "Deleted seller" : product.user?.username || "Unknown";

  return (
    <article className="product-card panel">
      <div className="product-card__header">
        <div className="product-card__accent" />
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-ink/40">
              Product #{product.id}
            </p>
            <h3 className="mt-3 font-display text-2xl font-bold text-ink">{product.name}</h3>
          </div>
          <StatusBadge tone={statusTones[product.productStatus] || "ink"}>
            {product.productStatus}
          </StatusBadge>
        </div>
      </div>

      <div className="product-card__body">
        <div className="product-card__summary">
          <div>
            <p className="text-sm leading-7 text-ink/68">
              {product.description || "No description provided for this auction yet."}
            </p>
          </div>
          <div className="product-card__price">
            <p className="text-xs font-semibold uppercase tracking-[0.22em] text-white/65">Current bid</p>
            <p className="mt-3 font-display text-3xl font-bold">{formatCurrency(product.currentBid)}</p>
            <p className="mt-1 text-sm text-white/70">Next minimum {formatCurrency(product.nextMinimumBid)}</p>
          </div>
        </div>

        <div className="product-card__meta">
          <InfoItem label="Minimum increment" value={formatCurrency(product.minimumIncrement)} />
          <InfoItem label="Bid count" value={product.bidCount ?? 0} />
          <InfoItem label="Seller" value={sellerLabel} />
          <InfoItem label="Starts" value={formatDateTime(product.startTime)} />
          <InfoItem label="Ends" value={formatDateTime(product.endTime)} />
        </div>

        <div className="mt-auto flex flex-wrap gap-3 pt-2">
          <Link to={`/products/${product.id}`} className="button-primary">
            {product.productStatus === "OPEN" ? "View auction" : "View details"}
          </Link>
        </div>
      </div>
    </article>
  );
}

function InfoItem({ label, value }) {
  return (
    <div className="product-card__meta-item">
      <p className="text-xs font-semibold uppercase tracking-[0.22em] text-ink/40">{label}</p>
      <p className="mt-2 text-sm font-semibold text-ink">{value}</p>
    </div>
  );
}
