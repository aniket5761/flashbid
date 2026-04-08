export default function StatCard({ label, value, tone = "ink" }) {
  const toneMap = {
    ink: "stat-card--ink",
    coral: "stat-card--coral",
    teal: "stat-card--teal"
  };

  return (
    <div className={`stat-card ${toneMap[tone] || toneMap.ink}`}>
      <div className="stat-card__inner">
        <p className="stat-card__label">{label}</p>
        <p className="stat-card__value">{value}</p>
      </div>
    </div>
  );
}
