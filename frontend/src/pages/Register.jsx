import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api, { getErrorMessage } from "../api/api";
import { useAuth } from "../state/AuthContext";
import Field from "../component/Field";
import PageHeader from "../component/PageHeader";

const initialForm = {
  firstName: "",
  lastName: "",
  username: "",
  email: "",
  password: "",
  confirmPassword: ""
};

export default function Register() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function update(key, value) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);

    try {
      const { confirmPassword, ...registerPayload } = form;
      const { data } = await api.post("/api/auth/register", registerPayload);
      login(data);
      navigate("/");
    } catch (submitError) {
      setError(getErrorMessage(submitError));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-5xl">
      <PageHeader
        eyebrow="Registration"
        title="Create your FlashBid account"
        description="Every new account starts as a bidder, and you can request seller access later from Seller Studio."
      />

      <div className="grid gap-6 lg:grid-cols-[0.92fr_1.08fr]">
        <aside className="panel px-8 py-8">
          <div className="eyebrow">What you get</div>
          <div className="mt-6 space-y-4">
            <Info title="Bidder-first onboarding" text="New accounts are created as standard users so seller and admin powers stay controlled." />
            <Info title="Immediate access" text="Successful registration logs you in automatically and opens the full bidder workflow." />
            <Info title="Seller path included" text="When you want to list products, request seller approval from the dedicated Seller Studio page." />
          </div>
          <p className="mt-8 text-sm text-ink/65">
            Already have an account?{" "}
            <Link to="/login" className="font-semibold text-ink underline underline-offset-4">
              Login instead
            </Link>
            .
          </p>
        </aside>

        <form onSubmit={handleSubmit} className="panel grid gap-5 px-8 py-8 md:grid-cols-2">
          <Field label="First name" value={form.firstName} onChange={(event) => update("firstName", event.target.value)} placeholder="Ava" />
          <Field label="Last name" value={form.lastName} onChange={(event) => update("lastName", event.target.value)} placeholder="Bennett" />
          <Field label="Username" value={form.username} onChange={(event) => update("username", event.target.value)} placeholder="ava.bids" />
          <Field label="Email" type="email" value={form.email} onChange={(event) => update("email", event.target.value)} placeholder="ava@example.com" />
          <Field
            label="Password"
            type="password"
            value={form.password}
            onChange={(event) => update("password", event.target.value)}
            placeholder="Minimum 8 characters"
          />
          <Field
            label="Confirm password"
            type="password"
            value={form.confirmPassword}
            onChange={(event) => update("confirmPassword", event.target.value)}
            placeholder="Re-enter your password"
          />

          {error ? <p className="md:col-span-2 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{error}</p> : null}

          <div className="md:col-span-2 flex justify-end">
            <button type="submit" className="button-primary min-w-44" disabled={loading}>
              {loading ? "Creating account..." : "Create account"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Info({ title, text }) {
  return (
    <div className="rounded-[22px] border border-ink/10 bg-sand/65 px-5 py-5">
      <h3 className="font-display text-2xl font-bold text-ink">{title}</h3>
      <p className="mt-2 text-sm leading-7 text-ink/65">{text}</p>
    </div>
  );
}
