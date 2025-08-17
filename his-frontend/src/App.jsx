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
  children,
  noCenterScroll = false, // NEU
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

      {/* Center */}
      <section className="app-center" aria-label="Zentralbereich">
        <div
          className={`center-scroll ${
            noCenterScroll ? "no-center-scroll" : ""
          }`}
        >
          {children}
        </div>
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

/* ========= Modul: Patientensuche ========= */

/** Gateway-Settings (Basic Auth analog App.jsx.old) */
const BASE_URL = "http://localhost:8080/api/v1";
const AUTH_HEADER = "Basic " + btoa("admin:dev-password"); // TODO: später aus Config/ENV laden
const PAGE_SIZE = 15;

/** dünner API-Wrapper */
async function apiRequest(endpoint, init = {}) {
  const res = await fetch(`${BASE_URL}${endpoint}`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: AUTH_HEADER,
      ...(init.headers || {}),
    },
    ...init,
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
  return res.json();
}

/** Debounce-Hook für Suche */
function useDebounced(value, delay = 350) {
  const [debounced, setDebounced] = React.useState(value);
  React.useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

/** Daten-Hook für Patientensuche (Sort-Stub vorhanden, aber noch ohne API-Weitergabe) */
function usePatientSearch({ query, page, sort }) {
  const debouncedQuery = useDebounced(query);
  const [rows, setRows] = React.useState([]);
  const [total, setTotal] = React.useState(0);
  const [pageCount, setPageCount] = React.useState(0);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState(null);

  React.useEffect(() => {
    const ctrl = new AbortController();
    setLoading(true);
    setError(null);

    // NEU: immer callen – auch bei leerem Query
    const q = (debouncedQuery ?? "").trim();

    apiRequest(
      `/patients/search?searchTerm=${encodeURIComponent(
        q
      )}&page=${page}&size=${PAGE_SIZE}`,
      { signal: ctrl.signal }
    )
      .then((data) => {
        // Erwartete Struktur: Page<PatientSummary>
        setRows(data.content || []);
        setTotal(
          typeof data.totalElements === "number" ? data.totalElements : 0
        );
        setPageCount(typeof data.totalPages === "number" ? data.totalPages : 0);
      })
      .catch((e) => {
        if (e.name !== "AbortError") setError(e);
      })
      .finally(() => setLoading(false));

    return () => ctrl.abort();
  }, [debouncedQuery, page /* sort (Stub) */]);

  return { rows, total, pageCount, loading, error };
}

/** Toolbar innerhalb des Pane (sticky im Scroll-Container) */
function PatientSearchSubToolbar({
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
        <button className="btn" onClick={onReset} disabled={!query}>
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

/** einfache Pagination unten */
function Pagination({ page, pageCount, onChange }) {
  return (
    <div className="pagination">
      <button
        className="btn"
        disabled={page <= 0}
        onClick={() => onChange(page - 1)}
      >
        ‹ Zurück
      </button>
      <span className="muted">
        Seite {page + 1} / {Math.max(pageCount, 1)}
      </span>
      <button
        className="btn"
        disabled={page + 1 >= pageCount}
        onClick={() => onChange(page + 1)}
      >
        Weiter ›
      </button>
    </div>
  );
}

/** Tabellenkomponente */
function PatientTable({ rows }) {
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
      <table
        className="table"
        role="table"
        aria-label="Suchergebnisse Patienten"
      >
        <thead>
          <tr>
            <th>Pat.-ID</th>
            <th>KVNR</th> {/* NEU */}
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
              </td>{" "}
              {/* colspan angepasst */}
            </tr>
          ) : (
            rows.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.kvnr || "—"}</td> {/* NEU */}
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

/** Page/Pane in „Dashboard“-Optik, volle Breite */
function ModulePatientSearch() {
  const [query, setQuery] = React.useState("");
  const [page, setPage] = React.useState(0);
  const [sort, setSort] = React.useState({ by: null, dir: "asc" }); // Sort-Stub

  const { rows, total, pageCount, loading, error } = usePatientSearch({
    query,
    page,
    sort,
  });

  // Bei Query-Änderung immer auf Seite 0 springen
  React.useEffect(() => {
    setPage(0);
  }, [query]);

  return (
    <div className="split-left" role="region" aria-label="Patientensuche">
      <div className="pane-title">Patientensuche</div>

      {/* Statischer Header: Suche/Info */}
      <PatientSearchSubToolbar
        query={query}
        onQueryChange={setQuery}
        total={total}
        loading={loading}
        onReset={() => setQuery("")}
      />

      {/* Statische Fehleranzeige */}
      {error && (
        <div className="alert alert-error">
          {String(error.message || error)}
        </div>
      )}

      {/* Nur die Ergebnisliste scrollt */}
      <div className="pane-scroll">
        {!error && <PatientTable rows={rows} />}
      </div>

      {/* Statischer Footer: Pagination */}
      {pageCount > 1 && (
        <div className="pane-footer">
          <Pagination page={page} pageCount={pageCount} onChange={setPage} />
        </div>
      )}
    </div>
  );
}

/** App-Rahmen mit Modulumschaltung per Tabs **/
export default function App() {
  const [active, setActive] = useState("patientSearch");
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
      {active !== "patientSearch" && (
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
      )}
      {active === "dashboard" && <ModuleDashboard />}
      {active === "patients" && <ModulePatientsSplit />}
      {active === "patientSearch" && <ModulePatientSearch />}
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
        noCenterScroll={active === "patientSearch"} // NEU
      >
        {centerContent}
      </MainLayout>

      {/* Fixer Footer */}
      <Footer />
    </div>
  );
}
