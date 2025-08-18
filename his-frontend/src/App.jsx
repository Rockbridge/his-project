import React from "react";
import "./App.css";

/**
 * Generischer Rahmen:
 * - Fixer Header (oben)
 * - Fixe Tab-Leiste (unter dem Header)
 * - Fixer Footer (unten)
 * - Dazwischen: Hauptbereich (Main) mit optionalen Sidebars
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
 */
function MainLayout({
  leftOpen,
  rightOpen,
  leftContent,
  rightContent,
  children,
  noCenterScroll = false,
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

/* ========= Modul: Patientensuche ========= */

/** Gateway-Settings (Basic Auth analog App.jsx.old) */
const BASE_URL = "http://localhost:8080/api/v1";
const AUTH_HEADER = "Basic " + btoa("admin:dev-password"); // TODO: später aus ENV laden
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

/** Daten-Hook für Patientensuche (Sort-Stub vorhanden, noch ohne API-Weitergabe) */
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

    const q = (debouncedQuery ?? "").trim();

    apiRequest(
      `/patients/search?searchTerm=${encodeURIComponent(
        q
      )}&page=${page}&size=${PAGE_SIZE}`,
      { signal: ctrl.signal }
    )
      .then((data) => {
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

/** Sub-Toolbar im Pane (statisch, nicht sticky) */
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

/** Pagination */
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
function PatientTable({ rows, onSelect, selectedId }) {
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

/** URL-Helpers */
function getSearchParams() {
  return new URLSearchParams(window.location.search);
}
function updateSearchParams(updates) {
  const params = new URLSearchParams(window.location.search);
  Object.entries(updates).forEach(([k, v]) => {
    if (
      v === undefined ||
      v === null ||
      v === "" ||
      (k === "page" && v === 0)
    ) {
      params.delete(k);
    } else {
      params.set(k, v);
    }
  });
  const newUrl = `${window.location.pathname}?${params.toString()}`;
  window.history.replaceState({}, "", newUrl);
}

/** Patientensuche: linke Spalte (Liste) – Details gehen in rechte Sidebar */
function ModulePatientSearch({ onSelect, selectedId }) {
  // Initialwerte aus URL
  const params = getSearchParams();
  const [query, setQuery] = React.useState(params.get("q") || "");
  const [page, setPage] = React.useState(
    parseInt(params.get("page") || "0", 10)
  );
  const [sort, setSort] = React.useState({ by: null, dir: "asc" }); // Stub

  const { rows, total, pageCount, loading, error } = usePatientSearch({
    query,
    page,
    sort,
  });

  // Query & Page in URL schreiben
  React.useEffect(() => {
    updateSearchParams({ q: query, page });
  }, [query, page]);

  // Reset page bei Query-Änderung
  React.useEffect(() => {
    setPage(0);
  }, [query]);

  return (
    <div className="split-left" role="region" aria-label="Patientensuche">
      {/* Kein pane-title, da im Tab enthalten */}
      <PatientSearchSubToolbar
        query={query}
        onQueryChange={setQuery}
        total={total}
        loading={loading}
        onReset={() => setQuery("")}
      />

      {error && (
        <div className="alert alert-error">
          {String(error.message || error)}
        </div>
      )}

      {/* Nur die Ergebnisliste scrollt */}
      <div className="pane-scroll">
        {!error && (
          <PatientTable
            rows={rows}
            onSelect={onSelect}
            selectedId={selectedId}
          />
        )}
      </div>

      {pageCount > 1 && (
        <div className="pane-footer">
          <Pagination page={page} pageCount={pageCount} onChange={setPage} />
        </div>
      )}
    </div>
  );
}

/** Detail-Logik & Sidebar-Komponente */

function usePatientDetail(patientId) {
  const [detail, setDetail] = React.useState(null);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState(null);

  React.useEffect(() => {
    if (!patientId) return;
    const ctrl = new AbortController();
    setLoading(true);
    setError(null);

    apiRequest(`/patients/${patientId}`, { signal: ctrl.signal })
      .then((data) => setDetail(data || null))
      .catch((e) => {
        if (e.name !== "AbortError") setError(e);
      })
      .finally(() => setLoading(false));

    return () => ctrl.abort();
  }, [patientId]);

  return { detail, loading, error };
}

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

function PatientDetailSidebar({ patientId }) {
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

/** App-Rahmen mit Modulumschaltung per Tabs **/
export default function App() {
  // --- URL-Sync: aktiver Tab (Default: patientSearch)
  const urlParams = new URLSearchParams(window.location.search);
  const [active, setActive] = React.useState(
    urlParams.get("tab") || "patientSearch"
  );

  // --- Sidebar-Zustände
  const [leftOpen, setLeftOpen] = React.useState(true);
  const [rightOpen, setRightOpen] = React.useState(false);

  // --- Auswahl für die rechte Sidebar (Patient-Details)
  const [selectedPatientId, setSelectedPatientId] = React.useState(null);

  // --- Tab → URL synchronisieren (?tab=...)
  React.useEffect(() => {
    const p = new URLSearchParams(window.location.search);
    p.set("tab", active);
    window.history.replaceState(
      {},
      "",
      `${window.location.pathname}?${p.toString()}`
    );
  }, [active]);

  // --- Patientensuche: rechte Sidebar automatisch öffnen/schließen
  React.useEffect(() => {
    if (active === "patientSearch") {
      setRightOpen(!!selectedPatientId);
    }
  }, [active, selectedPatientId]);

  // Sidebar-Inhalte
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

  const rightContent =
    active === "patientSearch" ? (
      <div className="sidebar-section">
        <div className="sidebar-header">
          <strong>Patient</strong>
          <button className="chip" onClick={() => setRightOpen(false)}>
            ×
          </button>
        </div>
        <div className="sidebar-body">
          <PatientDetailSidebar patientId={selectedPatientId} />
        </div>
      </div>
    ) : (
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

  // Center-Content (Toolbar global nur außerhalb Patientensuche)
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

      {active === "patientSearch" && (
        <ModulePatientSearch
          onSelect={setSelectedPatientId}
          selectedId={selectedPatientId}
        />
      )}

      {active === "dashboard" && <ModuleDashboard />}
      {active === "patients" && <ModulePatientsSplit />}
      {active === "reports" && <div className="row">Reports (Stub)</div>}
      {active === "settings" && <div className="row">Settings (Stub)</div>}
    </>
  );

  // --- Layout-Return
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
        noCenterScroll={active === "patientSearch"}
      >
        {centerContent}
      </MainLayout>

      {/* Fixer Footer */}
      <Footer />
    </div>
  );
}
