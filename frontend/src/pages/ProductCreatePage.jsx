import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { getErrorMessage, toApiDateTime } from "../api/api";
import Field from "../component/Field";
import PageHeader from "../component/PageHeader";

const initialForm = {
  name: "",
  startingPrice: "",
  minimumIncrement: "",
  startTime: "",
  endTime: "",
  description: ""
};

export default function ProductCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  function update(key, value) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      await api.post("/api/products", {
        ...form,
        startingPrice: Number(form.startingPrice),
        minimumIncrement: Number(form.minimumIncrement),
        startTime: toApiDateTime(form.startTime),
        endTime: toApiDateTime(form.endTime)
      });

      setSuccess("Auction created successfully.");
      setForm(initialForm);
      setTimeout(() => navigate("/products"), 900);
    } catch (submitError) {
      setError(getErrorMessage(submitError));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-5xl">
      <PageHeader
        eyebrow="Seller console"
        title="Create a new auction"
        description="Set the base price, minimum increment, and schedule for a listing your bidders can compete on."
      />

      <form onSubmit={handleSubmit} className="panel grid gap-5 px-8 py-8 md:grid-cols-2">
        <div className="md:col-span-2">
          <Field label="Product name" value={form.name} onChange={(event) => update("name", event.target.value)} placeholder="Rare vinyl collection" />
        </div>
        <Field
          label="Base price"
          type="number"
          min="1"
          value={form.startingPrice}
          onChange={(event) => update("startingPrice", event.target.value)}
          placeholder="500"
        />
        <Field
          label="Minimum increment"
          type="number"
          min="1"
          value={form.minimumIncrement}
          onChange={(event) => update("minimumIncrement", event.target.value)}
          placeholder="25"
        />
        <div className="rounded-[24px] border border-ink/10 bg-sand/55 px-5 py-5 text-sm leading-7 text-ink/65">
          Choose when the auction starts and ends. FlashBid will move it through scheduled, open, and closed states automatically.
        </div>
        <Field label="Start time" type="datetime-local" value={form.startTime} onChange={(event) => update("startTime", event.target.value)} />
        <Field label="End time" type="datetime-local" value={form.endTime} onChange={(event) => update("endTime", event.target.value)} />
        <div className="md:col-span-2">
          <Field
            as="textarea"
            label="Description"
            rows="6"
            value={form.description}
            onChange={(event) => update("description", event.target.value)}
            placeholder="Describe the item, seller notes, or bidding context."
            className="min-h-36 resize-y"
          />
        </div>

        {error ? <p className="md:col-span-2 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{error}</p> : null}
        {success ? <p className="md:col-span-2 rounded-2xl bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">{success}</p> : null}

        <div className="md:col-span-2 flex justify-end">
          <button type="submit" className="button-primary min-w-48" disabled={loading}>
            {loading ? "Publishing..." : "Create auction"}
          </button>
        </div>
      </form>
    </div>
  );
}
