import React, { useState } from "react";
import "./App.css";

/**
 * Generischer Rahmen:
 * - Fixer Header (oben)
 * - Fixe Tab-Leiste (unter dem Header)
 * - Fixer Footer (unten)
 * - Dazwischen: Hauptbereich (Main), dessen Layout je Modul variiert (mit/ohne Sidebars, Splits, eigene Scrollbereiche)
 */

function Header() {
  return (
    <header className="app-header" role="banner">
      <div className="app-logo">Hospital Information System</div>
      <div className="app-header-meta">
        <span className="app-user">Dr. Schmidt</span>
        <span className="app-role">Physician</span>
      </div>
    </header>
  );
}

function Tabs({ active, onChange }) {
  const tabs = [
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

function Footer() {
  return (
    <footer className="app-footer" role="contentinfo">
      <div className="footer-left">🟢 Realtime enabled</div>
      <div className="footer-right">© 2025 HIS Demo</div>
    </footer>
  );
}

/**
 * MainLayout: generischer Hauptbereich mit optionalen Sidebars (links/rechts)
 * - Nur dieser Bereich enthält Sidebars (nicht Header/Tabs/Footer)
 * - centerPane, leftSidebar, rightSidebar können unabhängig scrollen
 */
function MainLayout({
  leftOpen,
  rightOpen,
  leftContent,
  rightContent,
  children /* center content */,
}) {
  const cls = [
    "app-main",
    leftOpen ? "with-left" : "no-left",
    rightOpen ? "with-right" : "no-right",
  ].join(" ");

  return (
    <main className={cls} role="main" aria-label="Hauptbereich">
      {leftOpen && (
        <aside
          className="app-sidebar app-sidebar-left"
          aria-label="Linke Seitenleiste"
        >
          <div className="sidebar-scroll">{leftContent}</div>
        </aside>
      )}

      <section className="app-center" aria-label="Zentraler Inhaltsbereich">
        <div className="center-scroll">{children}</div>
      </section>

      {rightOpen && (
        <aside
          className="app-sidebar app-sidebar-right"
          aria-label="Rechte Seitenleiste"
        >
          <div className="sidebar-scroll">{rightContent}</div>
        </aside>
      )}
    </main>
  );
}

/** Beispiel-Module **/

function ModuleDashboard() {
  // Einspaltiges Pane – identisches Look&Feel wie "Patients" -> "Liste"
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
  // Demonstriert unabhängiges Scrolling in einem Split-Layout innerhalb des Center-Panes
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
  const [active, setActive] = useState("dashboard");
  const [leftOpen, setLeftOpen] = useState(true);
  const [rightOpen, setRightOpen] = useState(false);

  // Sidebar-Inhalte werden von Modulen bestimmt; hier Demo
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
            {leftOpen ? "Linke Sidebar ausblenden" : "Linke Sidebar einblenden"}
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
      {active === "reports" && (
        <div className="demo-block">
          <h2>Reports</h2>
          <p>Platzhalter-Inhalt …</p>
        </div>
      )}
      {active === "settings" && (
        <div className="demo-block">
          <h2>Settings</h2>
          <p>Platzhalter-Inhalt …</p>
        </div>
      )}
    </>
  );

  return (
    <div className="app-shell">
      {/* Fixe obere Bereiche */}
      <div className="top-fixed">
        <Header />
        <Tabs active={active} onChange={setActive} />
      </div>

      {/* Hauptbereich (mit optionalen Sidebars nur hier) */}
      <MainLayout
        leftOpen={leftOpen}
        rightOpen={rightOpen}
        leftContent={leftContent}
        rightContent={rightContent}
      >
        {centerContent}
      </MainLayout>

      {/* Fixer Footer */}
      <Footer />
    </div>
  );
}
