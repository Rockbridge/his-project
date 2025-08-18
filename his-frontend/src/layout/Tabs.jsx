import React from "react";

export default function Tabs({ active, onChange }) {
  const tabs = [
    { id: "patientSearch", label: "Patientensuche", icon: "🔎" },
    { id: "dashboard", label: "Dashboard", icon: "📊" },
    { id: "patients", label: "Patients", icon: "👥" },
    { id: "reports", label: "Reports", icon: "📋" },
    { id: "settings", label: "Settings", icon: "⚙️" },
  ];
  return (
    <nav className="app-tabs" role="navigation" aria-label="Module Tabs">
      {tabs.map((t) => (
        <button
          key={t.id}
          className={`app-tab ${active === t.id ? "active" : ""}`}
          onClick={() => onChange(t.id)}
          type="button"
        >
          <span className="tab-icon" aria-hidden>
            {t.icon}
          </span>
          {t.label}
        </button>
      ))}
    </nav>
  );
}
