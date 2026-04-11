import { useEffect, useState } from "react";
import api, { getErrorMessage } from "../api/api";
import { normalizePage } from "../api/format";
import EmptyState from "../component/EmptyState";
import Field from "../component/Field";
import PageHeader from "../component/PageHeader";
import Pagination from "../component/Pagination";
import UserCard from "../component/UserCard";
import StatusBadge from "../component/StatusBadge";

const initialPage = {
  content: [],
  number: 0,
  totalPages: 0,
  totalElements: 0
};

export default function Users() {
  const [users, setUsers] = useState(initialPage);
  const [filters, setFilters] = useState({
    page: 0,
    username: "",
    sortBy: "username"
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadUsers() {
      setLoading(true);
      setError("");
      try {
        const params = {
          page: filters.page,
          sortBy: filters.sortBy
        };

        if (filters.username) {
          params.username = filters.username;
        }

        const { data } = await api.get("/api/user/all", { params });
        setUsers(normalizePage(data));
      } catch (requestError) {
        setError(getErrorMessage(requestError));
        setUsers(initialPage);
      } finally {
        setLoading(false);
      }
    }

    loadUsers();
  }, [filters]);

  function updateFilter(key, value) {
    setFilters((current) => ({
      ...current,
      [key]: value,
      page: key === "page" ? value : 0
    }));
  }

  const userPage = normalizePage(users);

  return (
    <div>
      <PageHeader
        eyebrow="User directory"
        title="Explore the user base with paginated clarity"
        description="Browse platform users with quick username filtering, lightweight profile cards, and a layout that feels more like a real admin surface than a demo table."
      />

      <div className="mb-6 flex flex-wrap gap-2">
        <StatusBadge tone="ink">Sort: {filters.sortBy}</StatusBadge>
        {filters.username ? <StatusBadge tone="coral">Search: {filters.username}</StatusBadge> : null}
      </div>

      <section className="panel mb-8 grid gap-4 px-6 py-6 md:grid-cols-2">
        <Field label="Search by username" value={filters.username} onChange={(event) => updateFilter("username", event.target.value)} placeholder="seller01" />
        <Field as="select" label="Sort by" value={filters.sortBy} onChange={(event) => updateFilter("sortBy", event.target.value)}>
          <option value="username">Username</option>
          <option value="registrationDate">Registration date</option>
          <option value="email">Email</option>
        </Field>
      </section>

      {error ? <div className="mb-8 rounded-3xl bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">{error}</div> : null}

      {loading ? (
        <div className="panel px-8 py-16 text-center text-base font-medium text-ink/60">Loading user directory...</div>
      ) : userPage.content.length === 0 ? (
        <EmptyState title="No users found" description="Try another username search or register a new account from the dedicated onboarding page." />
      ) : (
        <>
          <div className="mb-5 text-sm text-ink/65">
            Showing <span className="font-semibold text-ink">{userPage.content.length}</span> users on this page across{" "}
            <span className="font-semibold text-ink">{userPage.totalElements}</span> total accounts.
          </div>
          <section className="grid gap-6 lg:grid-cols-2">
            {userPage.content.map((user) => (
              <UserCard key={user.id} user={user} />
            ))}
          </section>
          <Pagination page={userPage.number} totalPages={userPage.totalPages} onPageChange={(page) => updateFilter("page", page)} />
        </>
      )}
    </div>
  );
}
