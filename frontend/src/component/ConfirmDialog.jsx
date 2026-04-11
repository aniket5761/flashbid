export default function ConfirmDialog({
  open,
  tone = "default",
  title,
  description,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  busy = false,
  onConfirm,
  onCancel
}) {
  if (!open) {
    return null;
  }

  const confirmClassName = tone === "danger"
    ? "inline-flex min-w-32 items-center justify-center rounded-full bg-rose-600 px-5 py-3 text-sm font-bold text-white transition hover:bg-rose-700 disabled:cursor-not-allowed disabled:opacity-70"
    : "inline-flex min-w-32 items-center justify-center rounded-full bg-ink px-5 py-3 text-sm font-bold text-white transition hover:bg-slateblue disabled:cursor-not-allowed disabled:opacity-70";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/45 px-4 backdrop-blur-sm">
      <div className="panel w-full max-w-xl px-7 py-7">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="eyebrow">{tone === "danger" ? "Confirm action" : "Review changes"}</p>
            <h2 className="mt-4 font-display text-3xl font-bold text-ink">{title}</h2>
          </div>
          <button
            type="button"
            className="rounded-full border border-ink/10 px-3 py-2 text-sm font-semibold text-ink/65 transition hover:border-ink/20 hover:text-ink"
            onClick={onCancel}
            disabled={busy}
          >
            Close
          </button>
        </div>

        <p className="mt-4 text-sm leading-7 text-ink/65">{description}</p>

        <div className="mt-7 flex flex-wrap justify-end gap-3">
          <button
            type="button"
            className="button-secondary min-w-28"
            onClick={onCancel}
            disabled={busy}
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            className={confirmClassName}
            onClick={onConfirm}
            disabled={busy}
          >
            {busy ? "Working..." : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
