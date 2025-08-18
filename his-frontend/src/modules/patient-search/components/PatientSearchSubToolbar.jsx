import React from "react";
import Button from "../../../components/Button";

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
        <Button onClick={onReset} disabled={!query}>
          Zurücksetzen
        </Button>
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
