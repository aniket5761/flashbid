import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import api, { getErrorMessage } from "../api/api";
import { createAuctionListClient } from "../api/liveAuction";
import { normalizePage } from "../api/format";
import { useAuth } from "../state/AuthContext";
import EmptyState from "../component/EmptyState";
import Field from "../component/Field";
import PageHeader from "../component/PageHeader";
import Pagination from "../component/Pagination";
import ProductCard from "../component/ProductCard";
import StatusBadge from "../component/StatusBadge";

const initialPage = {
  content: [],
  number: 0,
  totalPages: 0,
  totalElements: 0
};

export default function ProductsPage() {
  const { user } = useAuth();
  const [products, setProducts] = useState(initialPage);
  const [filters, setFilters] = useState({
    page: 0,
    name: "",
    productStatus: "",
    sortBy: "createdAt",
    sortDir: "desc"
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const productPage = normalizePage(products);

  const productIds = useMemo(
    () => productPage.content.map((product) => product.id),
    [productPage.content]
  );
  const productIdsKey = useMemo(() => productIds.join(","), [productIds]);

  const loadProducts = useCallback(async (options = {}) => {
    const { preserveError = false } = options;
    setLoading(true);
    if (!preserveError) {
      setError("");
    }
    try {
      const params = {
        page: filters.page,
        sortBy: filters.sortBy,
        sortDir: filters.sortDir
      };

      if (filters.name) {
        params.name = filters.name;
      }

      if (filters.productStatus) {
        params.productStatus = filters.productStatus;
      }

      const { data } = await api.get("/api/products", { params });
      setProducts(normalizePage(data));
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      setProducts(initialPage);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  useEffect(() => {
    if (productIds.length === 0) {
      return undefined;
    }

    const client = createAuctionListClient(productIds, {
      onMessage: (event) => {
        const productId = event?.summary?.productId;
        if (!productId) {
          return;
        }

        setProducts((current) => ({
          ...normalizePage(current),
          content: normalizePage(current).content.map((product) => (
            product.id === productId
              ? {
                ...product,
                productStatus: event.summary.status,
                currentBid: event.summary.currentBid,
                nextMinimumBid: event.summary.nextMinimumBid,
                minimumIncrement: event.summary.minimumIncrement,
                bidCount: event.summary.bidCount,
                startTime: event.summary.startTime ?? product.startTime,
                endTime: event.summary.endTime ?? product.endTime
              }
              : product
          ))
        }));
      },
      onError: (socketError) => {
        console.error(socketError);
      }
    });

    return () => {
      client.deactivate();
    };
  }, [productIds, productIdsKey]);

  function updateFilter(key, value) {
    setFilters((current) => ({
      ...current,
      [key]: value,
      page: key === "page" ? value : 0
    }));
  }

  const canSell = user?.role === "SELLER" || user?.role === "ADMIN";

  return (
    <div>
      <PageHeader
        eyebrow="Marketplace"
        title="Browse live and scheduled auctions"
        description="Search the marketplace, inspect current bidding, and open listings to place bids or monitor auction activity."
        actions={
          canSell ? (
            <Link to="/products/new" className="button-primary">
              Create auction
            </Link>
          ) : null
        }
      />

      <section className="panel mb-8 grid gap-4 px-6 py-6 lg:grid-cols-4">
        <Field label="Search by name" value={filters.name} onChange={(event) => updateFilter("name", event.target.value)} placeholder="Vintage watch" />
        <Field as="select" label="Status" value={filters.productStatus} onChange={(event) => updateFilter("productStatus", event.target.value)}>
          <option value="">All statuses</option>
          <option value="SCHEDULED">Scheduled</option>
          <option value="OPEN">Open</option>
          <option value="CLOSED">Closed</option>
        </Field>
        <Field as="select" label="Sort field" value={filters.sortBy} onChange={(event) => updateFilter("sortBy", event.target.value)}>
          <option value="createdAt">Created date</option>
          <option value="name">Name</option>
          <option value="startTime">Start time</option>
          <option value="endTime">End time</option>
          <option value="startingPrice">Starting price</option>
        </Field>
        <Field as="select" label="Sort direction" value={filters.sortDir} onChange={(event) => updateFilter("sortDir", event.target.value)}>
          <option value="desc">Descending</option>
          <option value="asc">Ascending</option>
        </Field>
      </section>

      <section className="mb-8 flex flex-wrap gap-3">
        <StatusBadge tone="ink">Sort: {filters.sortBy}</StatusBadge>
        <StatusBadge tone={filters.productStatus === "OPEN" ? "teal" : filters.productStatus === "SCHEDULED" ? "amber" : filters.productStatus === "CLOSED" ? "slate" : "neutral"}>
          {filters.productStatus || "All statuses"}
        </StatusBadge>
        {filters.name ? <StatusBadge tone="coral">Search: {filters.name}</StatusBadge> : null}
      </section>

      {error ? <div className="mb-8 rounded-3xl bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">{error}</div> : null}

      {loading ? (
        <LoadingPanel text="Loading product marketplace..." />
      ) : productPage.content.length === 0 ? (
        <EmptyState
          title="No products matched this view"
          description="Try a broader search, switch status filters, or return when more auctions are available."
        />
      ) : (
        <>
          <div className="mb-5 text-sm text-ink/65">
            Showing <span className="font-semibold text-ink">{productPage.content.length}</span> products on this page across{" "}
            <span className="font-semibold text-ink">{productPage.totalElements}</span> total results.
          </div>
          <section className="grid gap-6 lg:grid-cols-2">
            {productPage.content.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </section>
          <Pagination page={productPage.number} totalPages={productPage.totalPages} onPageChange={(page) => updateFilter("page", page)} />
        </>
      )}
    </div>
  );
}

function LoadingPanel({ text }) {
  return <div className="panel px-8 py-16 text-center text-base font-medium text-ink/60">{text}</div>;
}
