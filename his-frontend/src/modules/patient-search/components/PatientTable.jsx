import React from "react";
import PatientRow from "./PatientRow";

export default function PatientTable({ rows, onSelect, selectedId }) {
  return (
    <div className="table-wrap">
      <table
        className="table"
        role="table"
        aria-label="Suchergebnisse Patienten"
      >
        <thead>
          <tr>
            <th>Pat.-ID</th>
            <th>KVNR</th>
            <th>Patientenname</th>
            <th>Geburtsdatum</th>
            <th>Versicherung</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={7} className="empty">
                Keine Daten
              </td>
            </tr>
          ) : (
            rows.map((p) => (
              <PatientRow
                key={p.id}
                patient={p}
                onSelect={onSelect}
                selectedId={selectedId}
              />
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
