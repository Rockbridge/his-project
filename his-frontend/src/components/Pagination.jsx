import React from "react";
import Button from "./Button";

export default function Pagination({ page, pageCount, onChange }) {
  return (
    <div className="pagination">
      <Button disabled={page <= 0} onClick={() => onChange(page - 1)}>
        ‹ Zurück
      </Button>
      <span className="muted">Seite {page + 1} / {Math.max(pageCount, 1)}</span>
      <Button disabled={page + 1 >= pageCount} onClick={() => onChange(page + 1)}>
        Weiter ›
      </Button>
    </div>
  );
}
