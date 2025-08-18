import React from "react";

export default function PatientTable({ rows, onSelect, selectedId }) {
  const fmtDate = (iso) => {
    try {
      if (!iso) return "—";
      const d = new Date(iso);
      if (isNaN(d.getTime())) return "—";
      return d.toLocaleDateString("de-DE");
    } catch {
      return "—";
    }
  };

  const mapGender = (g) => {
    switch (g) {
      case "MALE":
        return "männlich";
      case "FEMALE":
        return "weiblich";
      case "OTHER":
        return "divers";
      case "UNKNOWN":
      default:
        return "unbekannt";
    }
  };

  const mapStatus = (s) => {
    const m = {
      ACTIVE: "AKTIV",
      INACTIVE: "INAKTIV",
      SUSPENDED: "PAUSIERT",
      CANCELLED: "STORNIERT",
      UNKNOWN: "UNBEKANNT",
    };
    return m[s] || s || "—";
  };

  return (
    <div className="table-wrap">
      <table className="table" role="table" aria-label="Suchergebnisse Patienten">
        <thead>
          <tr>
            <th>Pat.-ID</th>
            <th>KVNR</th>
            <th>Patientenname</th>
            <th>Geburtsdatum</th>
            <th>Geschlecht</th>
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
              <tr
                key={p.id}
                onClick={() => onSelect && onSelect(p.id)}
                className={
                  "clickable-row" + (selectedId === p.id ? " selected" : "")
                }
              >
                <td>{p.id}</td>
                <td>{p.kvnr || "—"}</td>
                <td>{p.fullName || "—"}</td>
                <td>{fmtDate(p.birthDate)}</td>
                <td>{mapGender(p.gender)}</td>
                <td>{p.insuranceCompanyName || "—"}</td>
                <td>
                  <span
                    className={`badge badge-${(
                      p.insuranceStatus || "UNKNOWN"
                    ).toLowerCase()}`}
                  >
                    {mapStatus(p.insuranceStatus)}
                  </span>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
