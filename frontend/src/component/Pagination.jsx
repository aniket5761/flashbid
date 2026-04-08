export default function Pagination({ page, totalPages, onPageChange }) {
  const currentPage = page ?? 0;
  const safeTotal = totalPages ?? 0;

  if (safeTotal <= 1) {
    return null;
  }

  return (
    <div className="pagination-bar">
      <div className="text-sm text-ink/65">
        Page <span className="font-semibold text-ink">{currentPage + 1}</span> of{" "}
        <span className="font-semibold text-ink">{safeTotal}</span>
      </div>

      <div className="flex items-center gap-2">
        <button
          type="button"
          className="button-secondary !px-4 !py-2"
          disabled={currentPage === 0}
          onClick={() => onPageChange(currentPage - 1)}
        >
          Previous
        </button>
        <button
          type="button"
          className="button-primary !px-4 !py-2"
          disabled={currentPage >= safeTotal - 1}
          onClick={() => onPageChange(currentPage + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
}
