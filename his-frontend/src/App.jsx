import React from "react";
import "./App.css";
import { Header, Tabs, MainLayout, Footer } from "./layout";
import PatientSearchPage from "./modules/patient-search/PatientSearchPage";

/**
 * Generischer Rahmen:
 * - Fixer Header (oben)
 * - Fixe Tab-Leiste (unter dem Header)
 * - Fixer Footer (unten)
 * - Dazwischen: Hauptbereich (Main) mit optionalen Sidebars
 */

/** Beispiel-Module **/

function ModuleDashboard() {
  return (
    <div className="single-root">
      <div className="single-pane" role="region" aria-label="Dashboard – Liste">
        <div className="pane-title">Liste</div>
        <div className="pane-scroll">
          {Array.from({ length: 60 }).map((_, i) => (
            <div key={i} className="row">
              Eintrag {i + 1}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function ModulePatientsSplit() {
  return (
    <div className="split-root">
      <div className="split-left">
        <div className="pane-title">Liste</div>
        <div className="pane-scroll">
          {Array.from({ length: 60 }).map((_, i) => (
            <div key={i} className="row">
              Patient {i + 1}
            </div>
          ))}
        </div>
      </div>
      <div className="split-right">
        <div className="pane-title">Details</div>
        <div className="pane-scroll">
          <p>
            Langer Detailtext … (unabhängig scrollbar). Hier könnten
            Encounter-Details, SOAP-Notizen, etc. stehen.
          </p>
          {Array.from({ length: 30 }).map((_, i) => (
            <p key={i}>Absatz {i + 1}: Lorem ipsum dolor sit amet…</p>
          ))}
        </div>
      </div>
    </div>
  );
}

/** App-Rahmen mit Modulumschaltung per Tabs **/
export default function App() {
  // --- URL-Sync: aktiver Tab (Default: patientSearch)
  const urlParams = new URLSearchParams(window.location.search);
  const [active, setActive] = React.useState(
    urlParams.get("tab") || "patientSearch"
  );

  React.useEffect(() => {
    const p = new URLSearchParams(window.location.search);
    p.set("tab", active);
    window.history.replaceState(
      {},
      "",
      `${window.location.pathname}?${p.toString()}`
    );
  }, [active]);

  // --- Sidebars für andere Module
  const [leftOpen, setLeftOpen] = React.useState(true);
  const [rightOpen, setRightOpen] = React.useState(false);

  const leftContent = (
    <div className="sidebar-section">
      <div className="sidebar-header">
        <strong>Filter</strong>
        <button className="chip" onClick={() => setLeftOpen(false)}>
          ×
        </button>
      </div>
      <div className="sidebar-body">
        {Array.from({ length: 20 }).map((_, i) => (
          <label key={i} className="filter-row">
            <input type="checkbox" /> Option {i + 1}
          </label>
        ))}
      </div>
    </div>
  );

  const rightContent = (
    <div className="sidebar-section">
      <div className="sidebar-header">
        <strong>Werkzeuge</strong>
        <button className="chip" onClick={() => setRightOpen(false)}>
          ×
        </button>
      </div>
      <div className="sidebar-body">
        {Array.from({ length: 30 }).map((_, i) => (
          <div key={i} className="tool-row">
            Tool #{i + 1}
          </div>
        ))}
      </div>
    </div>
  );

  const centerContent = (
    <>
      <div className="toolbar-stub">
        <div className="toolbar-left">
          <input className="input" placeholder="Suchen…" />
          <button className="btn btn-primary">Neu</button>
        </div>
        <div className="toolbar-right">
          <button className="btn" onClick={() => setLeftOpen((v) => !v)}>
            {leftOpen
              ? "Linke Sidebar ausblenden"
              : "Linke Sidebar einblenden"}
          </button>
          <button className="btn" onClick={() => setRightOpen((v) => !v)}>
            {rightOpen
              ? "Rechte Sidebar ausblenden"
              : "Rechte Sidebar einblenden"}
          </button>
        </div>
      </div>

      {active === "dashboard" && <ModuleDashboard />}
      {active === "patients" && <ModulePatientsSplit />}
      {active === "reports" && <div className="row">Reports (Stub)</div>}
      {active === "settings" && <div className="row">Settings (Stub)</div>}
    </>
  );

  return (
    <div className="app-shell">
      {/* Fixe obere Bereiche */}
      <div className="top-fixed">
        <Header />
        <Tabs active={active} onChange={setActive} />
      </div>

      {/* Hauptbereich */}
      {active === "patientSearch" ? (
        <PatientSearchPage />
      ) : (
        <MainLayout
          leftOpen={leftOpen}
          rightOpen={rightOpen}
          leftContent={leftContent}
          rightContent={rightContent}
        >
          {centerContent}
        </MainLayout>
      )}

      {/* Fixer Footer */}
      <Footer />
    </div>
  );
}
