import React from "react";

export default function PatientSearchSubToolbar({
  query,
  onQueryChange,
  total,
  loading,
  onReset,
}) {
  return (
    <div className="subtoolbar">
      <div className="subtoolbar-left">
        <input
          className="input"
          placeholder="Name oder KVNR eingeben…"
          value={query}
          onChange={(e) => onQueryChange(e.target.value)}
          aria-label="Patientensuche"
        />
        <button
          className="btn btn-secondary"
          onClick={onReset}
          disabled={!query}
        >
          Zurücksetzen
        </button>

      </div>
      <div className="subtoolbar-right">
        {loading ? (
          <span className="muted">Laden…</span>
        ) : (
          <span className="muted">Treffer: {total}</span>
        )}
      </div>
    </div>
  );
}
