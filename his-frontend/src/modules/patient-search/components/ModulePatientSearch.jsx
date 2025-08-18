import React from "react";
import usePatientSearch from "../hooks/usePatientSearch";
import PatientSearchSubToolbar from "./PatientSearchSubToolbar";
import PatientTable from "./PatientTable";

function Pagination({ page, pageCount, onChange }) {
  return (
    <div className="pagination">
      <button className="btn" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        ‹ Zurück
      </button>
      <span className="muted">Seite {page + 1} / {Math.max(pageCount, 1)}</span>
      <button
        className="btn"
        disabled={page + 1 >= pageCount}
        onClick={() => onChange(page + 1)}
      >
        Weiter ›
      </button>
    </div>
  );
}

function getSearchParams() {
  return new URLSearchParams(window.location.search);
}

function updateSearchParams(updates) {
  const params = new URLSearchParams(window.location.search);
  Object.entries(updates).forEach(([k, v]) => {
    if (
      v === undefined ||
      v === null ||
      v === "" ||
      (k === "page" && v === 0)
    ) {
      params.delete(k);
    } else {
      params.set(k, v);
    }
  });
  const newUrl = `${window.location.pathname}?${params.toString()}`;
  window.history.replaceState({}, "", newUrl);
}

export default function ModulePatientSearch({ onSelect, selectedId }) {

  const params = getSearchParams();
  const [query, setQuery] = React.useState(params.get("q") || "");
  const [page, setPage] = React.useState(parseInt(params.get("page") || "0", 10));
  const [sort, _setSort] = React.useState({ by: null, dir: "asc" }); // Stub

  const { rows, total, pageCount, loading, error } = usePatientSearch({
    query,
    page,
    sort,
  });

  React.useEffect(() => {
    updateSearchParams({ q: query, page });
  }, [query, page]);

  React.useEffect(() => {
    setPage(0);
  }, [query]);

  return (
    <div className="split-left" role="region" aria-label="Patientensuche">
      <PatientSearchSubToolbar
        query={query}
        onQueryChange={setQuery}
        total={total}
        loading={loading}
        onReset={() => setQuery("")}
      />

      {error && (
        <div className="alert alert-error">{String(error.message || error)}</div>
      )}

      <div className="pane-scroll">
        {!error && (
          <PatientTable rows={rows} onSelect={onSelect} selectedId={selectedId} />
        )}
      </div>

      {pageCount > 1 && (
        <div className="pane-footer">
          <Pagination page={page} pageCount={pageCount} onChange={setPage} />
        </div>
      )}
    </div>
  );
}
