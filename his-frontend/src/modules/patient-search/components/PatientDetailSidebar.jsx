import React from "react";
import usePatientDetail from "../hooks/usePatientDetail";

function splitName(fullName) {
  if (!fullName) return { firstName: "—", lastName: "" };
  const parts = fullName.trim().split(/\s+/);
  if (parts.length === 1) return { firstName: parts[0], lastName: "" };
  const lastName = parts.pop();
  const firstName = parts.join(" ");
  return { firstName, lastName };
}

function formatDateDE(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  return isNaN(d) ? "—" : d.toLocaleDateString("de-DE");
}

function calcAge(birthIso) {
  if (!birthIso) return null;
  const b = new Date(birthIso);
  if (isNaN(b)) return null;
  const today = new Date();
  let age = today.getFullYear() - b.getFullYear();
  const m = today.getMonth() - b.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < b.getDate())) age--;
  return age;
}

export default function PatientDetailSidebar({ patientId }) {
  const { detail, loading, error } = usePatientDetail(patientId);

  if (!patientId)
    return (
      <div className="placeholder">Bitte wählen Sie einen Patienten aus.</div>
    );
  if (loading) return <div className="placeholder">Lade Patientendaten…</div>;
  if (error)
    return (
      <div className="alert alert-error">{String(error.message || error)}</div>
    );
  if (!detail) return null;

  const { firstName, lastName } = splitName(detail.fullName);
  const age = calcAge(detail.birthDate);
  const addr =
    Array.isArray(detail.addresses) && detail.addresses.length > 0
      ? detail.addresses[0]
      : null;

  return (
    <div className="patient-sidebar">
      {/* Kopf */}
      <div className="patient-header">
        <div className="patient-firstname">{firstName || "—"}</div>
        <div className="patient-lastname">{(lastName || "").toUpperCase()}</div>

        <div className="patient-age">
          {age !== null ? `Alter: ${age} Jahre` : "Alter: —"}
          {detail.birthDate ? ` (${formatDateDE(detail.birthDate)})` : ""}
        </div>

        <div className="patient-gender">{detail.gender || "—"}</div>
      </div>

      {/* Basis */}
      <div className="detail-section">
        <div className="section-title">Versicherung</div>
        <div className="kv">
          <span className="kv-key">KVNR:</span>
          <span className="kv-val">{detail.kvnr || "—"}</span>
          <span className="kv-key">Geschlecht:</span>
          <span className="kv-val">{detail.gender || "—"}</span>
          <span className="kv-key">Kasse:</span>
          <span className="kv-val">{detail.insuranceCompanyName || "—"}</span>
          <span className="kv-key">Status:</span>
          <span className="kv-val">{detail.insuranceStatus || "—"}</span>
        </div>
      </div>

      {/* Kontakt */}
      <div className="detail-section">
        <div className="section-title">Kontakt</div>
        <div className="kv">
          <span className="kv-key">Telefon:</span>
          <span className="kv-val">{detail.phone || "—"}</span>
          <span className="kv-key">E-Mail:</span>
          <span className="kv-val">{detail.email || "—"}</span>
        </div>
      </div>

      {/* Adresse */}
      <div className="detail-section">
        <div className="section-title">Adresse</div>
        <div className="kv">
          <span className="kv-key">Straße:</span>
          <span className="kv-val">
            {addr ? [addr.street, addr.houseNumber].join(" ") : "—"}
          </span>
          <span className="kv-key">PLZ / Ort:</span>
          <span className="kv-val">
            {addr ? [addr.postalCode, addr.city].join(" ") : "—"}
          </span>
          {(addr?.state || addr?.country) && (
            <>
              <span className="kv-key">Bundesland / Land:</span>
              <span className="kv-val">
                {[addr.state, addr.country].filter(Boolean).join(" / ")}
              </span>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
