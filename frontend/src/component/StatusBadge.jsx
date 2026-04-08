import { classNames } from "../api/format";

const toneMap = {
  neutral: "border-ink/10 bg-white/80 text-ink/70",
  ink: "border-ink/15 bg-ink/6 text-ink",
  coral: "border-coral/20 bg-coral/12 text-coral",
  teal: "border-teal/20 bg-teal/12 text-teal",
  amber: "border-amber-300/40 bg-amber-100/80 text-amber-800",
  rose: "border-rose-300/40 bg-rose-100/85 text-rose-800",
  slate: "border-slate-300/40 bg-slate-100/85 text-slate-700"
};

export default function StatusBadge({ children, tone = "neutral", className = "" }) {
  return (
    <span
      className={classNames(
        "inline-flex items-center rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em]",
        toneMap[tone] || toneMap.neutral,
        className
      )}
    >
      {children}
    </span>
  );
}
