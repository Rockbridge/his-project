import React from "react";

function fmtDate(iso) {
  try {
    return new Date(iso).toLocaleDateString("de-DE");
  } catch {
    return "—";
  }
}

function fmtTime(iso) {
  try {
    return new Date(iso).toLocaleTimeString("de-DE", {
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return "—";
  }
}

function mapType(t) {
  const m = {
    INITIAL: "Erstkontakt",
    CONSULTATION: "Konsultation",
    EMERGENCY: "Notfall",
    ROUTINE_CHECKUP: "Routine",
    FOLLOW_UP: "Nachsorge",
    SURGERY: "Operation",
    DIAGNOSTIC: "Diagnostik",
  };
  return m[t] || t || "—";
}

function mapStatus(s) {
  const m = {
    PLANNED: "Geplant",
    IN_PROGRESS: "Laufend",
    COMPLETED: "Abgeschlossen",
    CANCELLED: "Abgebrochen",
    NO_SHOW: "Nicht erschienen",
    POSTPONED: "Verschoben",
  };
  return m[s] || s || "Unbekannt";
}

function statusClass(s) {
  const m = {
    IN_PROGRESS: "in_progress",
    COMPLETED: "completed",
    CANCELLED: "cancelled",
  };
  return m[s] || "unknown";
}

export default function EncounterList({ encounters }) {
  if (!encounters || encounters.length === 0) {
    return <div className="his-no-encounters">Keine Encounter gefunden.</div>;
  }
  return (
    <div className="his-encounter-section">
      <div className="his-encounter-list">
        {encounters.map((e) => (
          <div className="his-encounter-row" key={e.id}>
            <div className="his-encounter-main">
              <div className="his-encounter-date">
                <strong>{fmtDate(e.encounterDate)}</strong>
                <span className="his-encounter-time">{fmtTime(e.encounterDate)}</span>
              </div>
              <div className="his-encounter-info">
                <span className="his-encounter-type">{mapType(e.type)}</span>
                <span className={`his-encounter-status-badge ${statusClass(e.status)}`}>
                  {mapStatus(e.status)}
                </span>
              </div>
            </div>
            <div className="his-encounter-details">
              <div className="his-encounter-detail">
                <span className="his-detail-label">Dokumente:</span>
                <span className="his-detail-value">
                  {e.documentationCount ?? 0}
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
