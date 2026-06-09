import "../styles/Pagination.css";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  const getPageNumbers = () => {
    const maxVisiblePages = 10;
    const half = Math.floor(maxVisiblePages / 2);

    let startPage = Math.max(1, currentPage - half);
    let endPage = startPage + maxVisiblePages - 1;

    if (endPage > totalPages) {
      endPage = totalPages;
      startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    return Array.from(
      { length: endPage - startPage + 1 },
      (_, index) => startPage + index
    );
  };

  return (
    <div className="common-pagination">
      <button type="button" onClick={() => onPageChange(1)} disabled={currentPage === 1}>
        «
      </button>

      <button type="button" onClick={() => onPageChange(currentPage - 1)} disabled={currentPage === 1}>
        ‹
      </button>

      {getPageNumbers().map((page) => (
        <button
          key={page}
          type="button"
          className={currentPage === page ? "is-active" : ""}
          onClick={() => onPageChange(page)}
        >
          {page}
        </button>
      ))}

      <button type="button" onClick={() => onPageChange(currentPage + 1)} disabled={currentPage === totalPages}>
        ›
      </button>

      <button type="button" onClick={() => onPageChange(totalPages)} disabled={currentPage === totalPages}>
        »
      </button>
    </div>
  );
}

export default Pagination;