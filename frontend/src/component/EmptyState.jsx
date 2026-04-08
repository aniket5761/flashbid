export default function EmptyState({ title, description }) {
  return (
    <div className="empty-state panel">
      <div className="mx-auto max-w-xl">
        <div className="eyebrow">Nothing here yet</div>
        <h3 className="empty-state__title">{title}</h3>
        <p className="empty-state__description">{description}</p>
      </div>
    </div>
  );
}
