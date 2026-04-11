import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api, { getErrorMessage } from "../api/api";
import { useAuth } from "../state/AuthContext";
import Field from "../component/Field";
import PageHeader from "../component/PageHeader";

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const { data } = await api.post("/api/auth/login", form);
      login(data);
      navigate("/");
    } catch (submitError) {
      setError(getErrorMessage(submitError));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader
        eyebrow="Authentication"
        title="Welcome back to FlashBid"
        description="Sign in to place bids, manage your profile, publish auctions, or moderate the marketplace."
      />

      <div className="panel overflow-hidden lg:grid lg:grid-cols-[0.95fr_1.05fr]">
        <div className="bg-ink px-8 py-10 text-white">
          <div className="eyebrow border-white/15! bg-white/10! text-white/70!">Secure access</div>
          <h2 className="mt-5 font-display text-4xl font-bold">Sign in and keep moving.</h2>
          <p className="mt-4 text-sm leading-7 text-white/72">
            Your session is stored locally and attached automatically to bidding, profile, seller, and admin actions.
          </p>
          <div className="mt-8 rounded-3xl border border-white/10 bg-white/5 px-5 py-5 text-sm text-white/70">
            Need a fresh account?{" "}
            <Link to="/register" className="font-semibold text-white underline underline-offset-4">
              Create one here
            </Link>
            .
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 px-8 py-10">
          <Field
            label="Username"
            value={form.username}
            onChange={(event) => setForm((current) => ({ ...current, username: event.target.value }))}
            placeholder="Enter your username"
          />
          <Field
            label="Password"
            type="password"
            value={form.password}
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
            placeholder="Enter your password"
          />
          {error ? <p className="rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{error}</p> : null}
          <button type="submit" className="button-primary w-full" disabled={loading}>
            {loading ? "Signing in..." : "Login"}
          </button>
        </form>
      </div>
    </div>
  );
}
