import { Link, NavLink, useLocation } from "react-router-dom";
import { useAuth } from "../state/AuthContext";
import { classNames } from "../api/format";
import StatusBadge from "./StatusBadge";

export default function AppShell({ children }) {
  const { isAuthenticated, user, logout } = useAuth();
  const location = useLocation();
  const canSell = user?.role === "SELLER" || user?.role === "ADMIN";

  const navItems = [
    { to: "/", label: "Overview" },
    { to: "/products", label: "Marketplace" },
    ...(isAuthenticated ? [{ to: "/profile", label: "My Profile" }] : []),
    ...(isAuthenticated ? [{ to: "/seller", label: "Seller Studio" }] : []),
    ...(canSell ? [{ to: "/products/new", label: "Create Auction" }] : []),
    ...(user?.role === "ADMIN"
      ? [
          { to: "/admin", label: "Admin Dashboard" },
          { to: "/users", label: "Users" }
        ]
      : [])
  ];

  return (
    <div className="min-h-screen bg-grid bg-[size:34px_34px]">
      <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <header className="app-shell panel">
          <div className="app-shell__brand">
            <div className="eyebrow">Live auction workspace</div>
            <Link to="/" className="app-shell__logo">
              FlashBid
            </Link>
            <p className="app-shell__copy">
              Live auction workspace for bidding, selling, and marketplace moderation.
            </p>
          </div>

          <div className="app-shell__nav-area">
            <nav className="app-shell__nav">
              {navItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    classNames(
                      "app-shell__nav-link",
                      isActive || location.pathname === item.to ? "app-shell__nav-link--active" : ""
                    )
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>

            <div className="app-shell__actions">
              {isAuthenticated ? (
                <>
                  <div className="app-shell__identity">
                    <div>
                      <p className="app-shell__identity-name">{user?.username}</p>
                      <p className="app-shell__identity-role">{user?.role}</p>
                    </div>
                    {user?.sellerRequested ? <StatusBadge tone="amber">Pending seller approval</StatusBadge> : null}
                  </div>
                  <button type="button" onClick={logout} className="button-secondary">
                    Logout
                  </button>
                </>
              ) : (
                <>
                  <Link to="/login" className="button-secondary">
                    Login
                  </Link>
                  <Link to="/register" className="button-primary">
                    Create account
                  </Link>
                </>
              )}
            </div>
          </div>
        </header>

        <main className="pb-10 pt-8">{children}</main>
      </div>
    </div>
  );
}
