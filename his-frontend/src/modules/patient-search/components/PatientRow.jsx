import React from "react";
import usePatientEncounters from "../hooks/usePatientEncounters";
import EncounterList from "./EncounterList";

export default function PatientRow({ patient, onSelect, selectedId }) {
  const { encounters } = usePatientEncounters(patient.id);
  const [open, setOpen] = React.useState(false);

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

  const hasEncounters = encounters.length > 0;

  const toggleOpen = (e) => {
    e.stopPropagation();
    setOpen((o) => !o);
  };

  return (
    <>
      <tr
        onClick={() => onSelect && onSelect(patient.id)}
        className={"clickable-row" + (selectedId === patient.id ? " selected" : "")}
      >
        <td>
          {hasEncounters && (
            <button
              className="his-btn his-btn-sm" style={{ marginRight: "4px" }}
              aria-label={open ? "Encounter ausblenden" : "Encounter anzeigen"}
              onClick={toggleOpen}
            >
              {open ? "▼" : "▶"}
            </button>
          )}
          {patient.id}
        </td>
        <td>{patient.kvnr || "—"}</td>
        <td>{patient.fullName || "—"}</td>
        <td>{fmtDate(patient.birthDate)}</td>
        <td>{mapGender(patient.gender)}</td>
        <td>{patient.insuranceCompanyName || "—"}</td>
        <td>
          <span
            className={`badge badge-${(
              patient.insuranceStatus || "UNKNOWN"
            ).toLowerCase()}`}
          >
            {mapStatus(patient.insuranceStatus)}
          </span>
        </td>
      </tr>
      {open && hasEncounters && (
        <tr className="expanded-row">
          <td colSpan={7}>
            <EncounterList encounters={encounters} />
          </td>
        </tr>
      )}
    </>
  );
}
