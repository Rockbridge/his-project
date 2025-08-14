import React, { useState, useEffect, createContext, useContext } from "react";
import "./App.css";

// ===== CONTEXT & STATE MANAGEMENT =====
const HISContext = createContext();

const HISProvider = ({ children }) => {
  const [patients, setPatients] = useState([]);
  const [encounters, setEncounters] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [selectedEncounters, setSelectedEncounters] = useState([]);
  const [expandedRows, setExpandedRows] = useState(new Set());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [user, setUser] = useState({ name: "Dr. Schmidt", role: "Physician" });
  const [activeTab, setActiveTab] = useState("patients");
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(25);
  const [showConfigPanel, setShowConfigPanel] = useState(false);
  const [currentDateTime, setCurrentDateTime] = useState(new Date());

  const BASE_URL = "http://localhost:8080/api/v1";
  const AUTH_HEADER = "Basic " + btoa("admin:dev-password");

  // API Request Helper
  const apiRequest = async (endpoint, options = {}) => {
    try {
      const response = await fetch(`${BASE_URL}${endpoint}`, {
        headers: {
          "Content-Type": "application/json",
          Authorization: AUTH_HEADER,
          ...options.headers,
        },
        ...options,
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      return await response.json();
    } catch (error) {
      console.error("API Request Error:", error);
      throw error;
    }
  };

  // ✅ FIXED: Load Patient Encounters with correct URL and robust error handling
  const loadPatientEncounters = async (patientId) => {
    console.log(`🔍 Loading encounters for patient: ${patientId}`);

    try {
      // ✅ KORREKTE URL: /encounters/patient/... (BASE_URL enthält bereits /api/v1)
      const data = await apiRequest(
        `/encounters/patient/${patientId}?page=0&size=10`
      );
      console.log(`✅ Encounter API Response for ${patientId}:`, data);

      // API gibt Page-Objekt zurück mit content Array
      if (data && data.content && Array.isArray(data.content)) {
        console.log(
          `✅ Found ${data.content.length} encounters for patient ${patientId}`
        );
        return data.content;
      } else {
        console.log(`ℹ️ No encounters found for patient ${patientId}`);
        return [];
      }
    } catch (err) {
      console.error(
        `❌ Error loading encounters for patient ${patientId}:`,
        err
      );
      return []; // ✅ Return empty array instead of throwing
    }
  };

  // Load Patients
  const loadPatients = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiRequest(
        "/patients/search?searchTerm=&page=0&size=100"
      );
      console.log("API Response:", data);

      const enhancedPatients = await Promise.all(
        (data.content || []).map(async (patient, index) => {
          // ✅ Robuste Encounter Loading mit Debug Logging
          let encounters = [];
          try {
            encounters = await loadPatientEncounters(patient.id);
            console.log(
              `Patient ${patient.patientName || patient.fullName}: ${
                encounters.length
              } encounters loaded`
            );
          } catch (err) {
            console.warn(
              `Failed to load encounters for patient ${patient.id}:`,
              err
            );
            encounters = [];
          }

          const lastEncounter = encounters.length > 0 ? encounters[0] : null;

          // ✅ EINFACHE NAMEN-LOGIK: Verwende fullName direkt
          let firstName = patient.firstName || "";
          let lastName = patient.lastName || "";
          let patientName = patient.fullName;

          // Wenn kein fullName, konstruiere aus firstName/lastName
          if (!patientName && (firstName || lastName)) {
            patientName = lastName ? `${lastName}, ${firstName}` : firstName;
          }

          // Wenn gar keine Namen, verwende Fallback
          if (!patientName) {
            const demoNames = [
              "Max Mustermann",
              "Anna Schmidt",
              "Thomas Weber",
              "Maria Müller",
              "Klaus Fischer",
              "Petra Meyer",
              "Hans Wagner",
              "Sabine Becker",
            ];
            patientName =
              demoNames[index % demoNames.length] || `Patient #${index + 1}`;
          }

          // Extrahiere firstName/lastName aus fullName falls nötig
          if (patientName && (!firstName || !lastName)) {
            const parts = patientName
              .replace(/^(Dr\.|Prof\.|Herr|Frau)\s+/i, "")
              .trim()
              .split(" ");
            if (parts.length >= 2) {
              if (!firstName) firstName = parts[0];
              if (!lastName) lastName = parts.slice(1).join(" ");
            } else if (parts.length === 1) {
              if (!firstName && !lastName) firstName = parts[0];
            }
          }

          console.log(
            `Patient ${index}: "${patientName}" with ${encounters.length} encounters (from: firstName="${patient.firstName}", lastName="${patient.lastName}", fullName="${patient.fullName}")`
          );

          return {
            id: patient.id,
            firstName: firstName,
            lastName: lastName,
            fullName: patient.fullName,
            patientName: patientName,
            birthDate: patient.birthDate || "1980-01-01",
            gender: patient.gender || "UNKNOWN",
            kvnr: patient.kvnr || `DEMO${index}`,
            phone: patient.phone || "+49 30 12345678",
            email: patient.email || `patient${index}@demo.com`,
            age: patient.birthDate
              ? calculateAge(patient.birthDate)
              : calculateAge("1980-01-01"),
            lastEncounter: lastEncounter ? lastEncounter.encounterDate : null,
            encounters: encounters, // ✅ Sollte jetzt Daten enthalten!
            status: encounters.some((e) => e.status === "IN_PROGRESS")
              ? "in_progress"
              : "active",
          };
        })
      );

      console.log(
        "✅ Enhanced patients with encounters:",
        enhancedPatients.map((p) => ({
          id: p.id.substring(0, 8),
          name: p.patientName,
          encounters: p.encounters.length, // ✅ Debug: Zeige Encounter Count
        }))
      );
      setPatients(enhancedPatients);
    } catch (err) {
      console.error("Load patients error:", err);
      setError("Fehler beim Laden der Patienten: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Toggle Row Expansion
  const toggleRow = (index) => {
    const newExpandedRows = new Set(expandedRows);
    if (newExpandedRows.has(index)) {
      newExpandedRows.delete(index);
    } else {
      newExpandedRows.add(index);
    }
    setExpandedRows(newExpandedRows);
  };

  // Calculate Age
  const calculateAge = (birthDate) => {
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (
      monthDiff < 0 ||
      (monthDiff === 0 && today.getDate() < birth.getDate())
    ) {
      age--;
    }
    return age;
  };

  // Search Patients
  const handleSearch = async (term) => {
    setSearchTerm(term);
    setCurrentPage(1);

    if (!term.trim()) {
      loadPatients();
      return;
    }

    setLoading(true);
    try {
      const data = await apiRequest(
        `/patients/search?searchTerm=${encodeURIComponent(
          term
        )}&page=0&size=100`
      );
      console.log("Search API Response:", data); // Debug log

      const enhancedPatients = await Promise.all(
        (data.content || []).map(async (patient, index) => {
          console.log("Processing search patient:", patient); // Debug log

          // ✅ Robuste Encounter Loading auch für Search
          let encounters = [];
          try {
            encounters = await loadPatientEncounters(patient.id);
            console.log(
              `Search Patient ${patient.patientName || patient.fullName}: ${
                encounters.length
              } encounters loaded`
            );
          } catch (err) {
            console.warn(
              `Failed to load encounters for search patient ${patient.id}:`,
              err
            );
            encounters = [];
          }

          const lastEncounter = encounters.length > 0 ? encounters[0] : null;

          // ✅ NAMEN-FIX: Verwende fullName direkt, da Search API nur fullName liefert
          let firstName = patient.firstName || "";
          let lastName = patient.lastName || "";
          let patientName = patient.fullName; // API gibt fullName zurück, nicht firstName/lastName

          // Wenn kein fullName, konstruiere aus firstName/lastName (falls vorhanden)
          if (!patientName && (firstName || lastName)) {
            patientName = lastName ? `${lastName}, ${firstName}` : firstName;
          }

          // Wenn gar keine Namen, verwende Fallback
          if (!patientName) {
            patientName = `Patient ${patient.id?.substring(0, 8) || "Unknown"}`;
          }

          // Extrahiere firstName/lastName aus fullName falls nötig (für andere Komponenten)
          if (patientName && (!firstName || !lastName)) {
            const parts = patientName
              .replace(/^(Dr\.|Prof\.|Herr|Frau)\s+/i, "")
              .trim()
              .split(" ");
            if (parts.length >= 2) {
              if (!firstName) firstName = parts[0];
              if (!lastName) lastName = parts.slice(1).join(" ");
            } else if (parts.length === 1) {
              if (!firstName && !lastName) firstName = parts[0];
            }
          }

          console.log(
            `Search Patient ${index}: "${patientName}" with ${encounters.length} encounters (from fullName: "${patient.fullName}", firstName: "${patient.firstName}", lastName: "${patient.lastName}")`
          );

          return {
            id: patient.id,
            firstName: firstName,
            lastName: lastName,
            fullName: patient.fullName,
            patientName: patientName, // ✅ Dieser wird jetzt korrekt gesetzt
            birthDate: patient.birthDate,
            gender: patient.gender,
            kvnr: patient.kvnr,
            phone: patient.phone,
            email: patient.email,
            age: patient.birthDate
              ? calculateAge(patient.birthDate)
              : "Unbekannt",
            lastEncounter: lastEncounter ? lastEncounter.encounterDate : null,
            encounters: encounters, // ✅ Sollte jetzt auch in Search Daten enthalten!
            status: encounters.some((e) => e.status === "IN_PROGRESS")
              ? "in_progress"
              : "active",
          };
        })
      );

      console.log(
        "✅ Enhanced search patients with encounters:",
        enhancedPatients.map((p) => ({
          id: p.id.substring(0, 8),
          name: p.patientName,
          encounters: p.encounters.length, // ✅ Debug: Zeige Encounter Count
        }))
      );
      setPatients(enhancedPatients);
    } catch (err) {
      console.error("Search patients error:", err); // Debug log
      setError("Fehler beim Suchen: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Update DateTime
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentDateTime(new Date());
    }, 60000);

    return () => clearInterval(timer);
  }, []);

  // Initial Load
  useEffect(() => {
    loadPatients();
  }, []);

  return (
    <HISContext.Provider
      value={{
        patients,
        encounters,
        selectedPatient,
        selectedEncounters,
        expandedRows,
        loading,
        error,
        user,
        activeTab,
        searchTerm,
        currentPage,
        itemsPerPage,
        showConfigPanel,
        currentDateTime,
        loadPatients,
        loadPatientEncounters,
        toggleRow,
        handleSearch,
        setActiveTab,
        setCurrentPage,
        setItemsPerPage,
        setShowConfigPanel,
        setError,
      }}
    >
      {children}
    </HISContext.Provider>
  );
};

// ===== HEADER COMPONENT =====
const Header = () => {
  const { user, currentDateTime, setShowConfigPanel } = useContext(HISContext);

  const formatDateTime = (date) => {
    const options = {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    };
    return date.toLocaleDateString("de-DE", options);
  };

  return (
    <header className="his-header">
      <div className="his-header-left">
        <div className="his-logo">
          <span className="icon icon-hospital"></span>
          Hospital Information System
        </div>
        <button
          className="his-menu-button"
          onClick={() => setShowConfigPanel(true)}
        >
          <span className="icon icon-settings"></span>
          Konfiguration
        </button>
      </div>
      <div className="his-header-right">
        <div className="his-datetime">
          <span className="icon icon-calendar"></span>
          <span>{formatDateTime(currentDateTime)}</span>
        </div>
        <div className="his-user-info">
          <span className="icon icon-user"></span>
          {user.name}
        </div>
      </div>
    </header>
  );
};

// ===== NAVIGATION COMPONENT =====
const Navigation = () => {
  const { activeTab, setActiveTab } = useContext(HISContext);

  const tabs = [
    { id: "patients", label: "Patienten" },
    { id: "encounters", label: "Begegnungen" },
    { id: "appointments", label: "Termine" },
    { id: "reports", label: "Berichte" },
    { id: "administration", label: "Administration" },
  ];

  return (
    <nav className="his-nav-tabs">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          className={`his-nav-tab ${activeTab === tab.id ? "active" : ""}`}
          onClick={() => setActiveTab(tab.id)}
        >
          {tab.label}
        </button>
      ))}
    </nav>
  );
};

// ===== TOOLBAR COMPONENT =====
const Toolbar = () => {
  const { searchTerm, handleSearch, loadPatients } = useContext(HISContext);

  return (
    <div className="his-toolbar">
      <div className="his-toolbar-left">
        <div className="his-search-container">
          <span className="icon icon-search his-search-icon"></span>
          <input
            type="text"
            className="his-search-input"
            placeholder="Patienten suchen..."
            value={searchTerm}
            onChange={(e) => handleSearch(e.target.value)}
          />
        </div>
        <button className="his-btn his-btn-secondary">
          <span className="icon icon-filter"></span>
          Filter
        </button>
        <button
          className="his-btn his-btn-secondary"
          onClick={() =>
            document.querySelector(".his-config-panel").classList.toggle("open")
          }
        >
          <span className="icon icon-settings"></span>
          Spalten
        </button>
      </div>
      <div className="his-section-meta">
        <span className="icon icon-react"></span>
        Echtzeitdaten aktiviert
        <button className="his-btn his-btn-secondary" onClick={loadPatients}>
          <span className="icon icon-refresh"></span>
          Aktualisieren
        </button>
      </div>
    </div>
  );
};

// ===== PATIENT LIST COMPONENT =====
const PatientList = () => {
  const {
    patients,
    loading,
    error,
    expandedRows,
    toggleRow,
    currentPage,
    itemsPerPage,
  } = useContext(HISContext);

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    return new Date(dateString).toLocaleDateString("de-DE");
  };

  const getStatusText = (status) => {
    const statusMap = {
      active: "Aktiv",
      in_progress: "In Behandlung",
      completed: "Abgeschlossen",
      cancelled: "Storniert",
    };
    return statusMap[status] || status;
  };

  const getGenderText = (gender) => {
    const genderMap = {
      MALE: "M",
      FEMALE: "W",
      DIVERSE: "D",
    };
    return genderMap[gender] || gender;
  };

  // Pagination
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = Math.min(startIndex + itemsPerPage, patients.length);
  const pagePatients = patients.slice(startIndex, endIndex);

  if (loading) {
    return (
      <div className="his-loading">
        <div className="loading-spinner"></div>
        <p>Lade Patienten...</p>
      </div>
    );
  }

  return (
    <>
      {error && (
        <div className="his-error-message">
          <span className="error-icon">⚠️</span>
          {error}
        </div>
      )}

      <div className="his-list-container">
        <table className="his-list">
          <thead>
            <tr>
              <th className="his-icon-column"></th>
              <th style={{ width: "15%" }}>Patient-ID</th>
              <th style={{ width: "25%" }}>Name</th>
              <th style={{ width: "12%" }}>Geburtsdatum</th>
              <th style={{ width: "10%" }}>Alter</th>
              <th style={{ width: "8%" }}>Geschlecht</th>
              <th style={{ width: "12%" }}>Status</th>
              <th style={{ width: "18%" }}>Letzte Begegnung</th>
            </tr>
          </thead>
          <tbody>
            {pagePatients.map((patient, index) => {
              const actualIndex = startIndex + index;
              const isExpanded = expandedRows.has(actualIndex);

              return (
                <React.Fragment key={patient.id}>
                  <tr
                    className={`his-list-row ${isExpanded ? "expanded" : ""}`}
                    onClick={() => toggleRow(actualIndex)}
                  >
                    <td className="his-icon-column">
                      <span
                        className={`his-expand-icon icon-chevron ${
                          isExpanded ? "expanded" : ""
                        }`}
                      ></span>
                    </td>
                    <td>{patient.kvnr || patient.id}</td>
                    <td>
                      <strong>{patient.patientName}</strong>
                    </td>
                    <td>{formatDate(patient.birthDate)}</td>
                    <td>{patient.age}</td>
                    <td>{getGenderText(patient.gender)}</td>
                    <td>
                      <span className={`his-status ${patient.status}`}>
                        {getStatusText(patient.status)}
                      </span>
                    </td>
                    <td>{formatDate(patient.lastEncounter)}</td>
                  </tr>

                  {isExpanded && (
                    <tr className="his-child-row show">
                      <td colSpan="8">
                        <EncounterDetails patient={patient} />
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              );
            })}
          </tbody>
        </table>

        {patients.length === 0 && !loading && (
          <div className="his-empty-state">
            <span className="empty-icon">📋</span>
            <h3>Keine Patienten gefunden</h3>
            <p>Keine Patienten entsprechen den Suchkriterien</p>
          </div>
        )}
      </div>
    </>
  );
};

// ===== ENCOUNTER DETAILS COMPONENT =====
const EncounterDetails = ({ patient }) => {
  const encounters = patient.encounters || [];

  if (encounters.length === 0) {
    return (
      <div
        style={{
          padding: "20px",
          textAlign: "center",
          color: "var(--text-secondary)",
        }}
      >
        <p>Keine Encounters für diesen Patienten verfügbar</p>
      </div>
    );
  }

  const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return "Unbekannt";
    return new Date(dateTimeString).toLocaleString("de-DE");
  };

  const getTypeIcon = (type) => {
    const icons = {
      INITIAL: "🆕",
      CONSULTATION: "👨‍⚕️",
      EMERGENCY: "🚨",
      FOLLOW_UP: "🔄",
      ROUTINE: "📝",
    };
    return icons[type] || "📋";
  };

  const getStatusColor = (status) => {
    const colors = {
      PLANNED: "#059669",
      IN_PROGRESS: "#d97706",
      COMPLETED: "#2563eb",
      CANCELLED: "#dc2626",
    };
    return colors[status] || "#6b7280";
  };

  return (
    <div style={{ padding: "20px" }}>
      <h4 style={{ marginBottom: "15px", color: "var(--text-primary)" }}>
        📋 Encounters ({encounters.length})
      </h4>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(350px, 1fr))",
          gap: "15px",
        }}
      >
        {encounters.slice(0, 3).map((encounter, index) => (
          <div
            key={encounter.id || index}
            style={{
              background: "var(--bg-primary)",
              border: "1px solid var(--border-color)",
              borderRadius: "8px",
              padding: "15px",
              borderLeft: `4px solid ${getStatusColor(encounter.status)}`,
            }}
          >
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "10px",
              }}
            >
              <div
                style={{ display: "flex", alignItems: "center", gap: "8px" }}
              >
                <span style={{ fontSize: "1.2em" }}>
                  {getTypeIcon(encounter.type)}
                </span>
                <strong>{encounter.type || "Unbekannt"}</strong>
              </div>
              <span
                style={{
                  padding: "4px 8px",
                  borderRadius: "12px",
                  fontSize: "0.8em",
                  fontWeight: "600",
                  color: "white",
                  backgroundColor: getStatusColor(encounter.status),
                }}
              >
                {encounter.status || "UNKNOWN"}
              </span>
            </div>

            <div style={{ fontSize: "0.9em", color: "var(--text-secondary)" }}>
              <div style={{ marginBottom: "5px" }}>
                <strong>📅 Datum:</strong>{" "}
                {formatDateTime(encounter.encounterDate)}
              </div>
              {encounter.practitionerId && (
                <div style={{ marginBottom: "5px" }}>
                  <strong>👨‍⚕️ Arzt:</strong>{" "}
                  {encounter.practitionerId.substring(0, 8)}...
                </div>
              )}
              {encounter.reason && (
                <div style={{ marginBottom: "5px" }}>
                  <strong>📝 Grund:</strong> {encounter.reason}
                </div>
              )}
              {encounter.billingContext && (
                <div>
                  <strong>💳 Abrechnung:</strong> {encounter.billingContext}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {encounters.length > 3 && (
        <div
          style={{
            marginTop: "15px",
            textAlign: "center",
            color: "var(--text-secondary)",
            fontSize: "0.9em",
          }}
        >
          ... und {encounters.length - 3} weitere Encounters
        </div>
      )}
    </div>
  );
};

// ===== PAGINATOR COMPONENT =====
const Paginator = () => {
  const {
    patients,
    currentPage,
    itemsPerPage,
    setCurrentPage,
    setItemsPerPage,
  } = useContext(HISContext);

  const totalPages = Math.ceil(patients.length / itemsPerPage);
  const startItem = (currentPage - 1) * itemsPerPage + 1;
  const endItem = Math.min(currentPage * itemsPerPage, patients.length);

  const goToPage = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const changeItemsPerPage = (newItemsPerPage) => {
    setItemsPerPage(newItemsPerPage);
    setCurrentPage(1);
  };

  // Generate page numbers
  const getPageNumbers = () => {
    const pages = [];
    const maxVisiblePages = 7;

    let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    // Add first page if not in range
    if (startPage > 1) {
      pages.push(1);
      if (startPage > 2) {
        pages.push("...");
      }
    }

    // Add visible pages
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    // Add last page if not in range
    if (endPage < totalPages) {
      if (endPage < totalPages - 1) {
        pages.push("...");
      }
      pages.push(totalPages);
    }

    return pages;
  };

  if (patients.length === 0) return null;

  return (
    <div className="his-paginator-container">
      <div className="his-paginator">
        <div className="his-paginator-info">
          <span className="his-items-info">
            Zeige{" "}
            <strong>
              {startItem}-{endItem}
            </strong>{" "}
            von <strong>{patients.length}</strong> Patienten
          </span>
        </div>

        <div className="his-paginator-controls">
          <button
            className="his-paginator-btn his-paginator-btn-prev"
            disabled={currentPage === 1}
            onClick={() => goToPage(currentPage - 1)}
          >
            <span className="icon icon-chevron-left"></span>
            Zurück
          </button>

          <div className="his-paginator-pages">
            {getPageNumbers().map((page, index) =>
              page === "..." ? (
                <span key={index} className="his-paginator-ellipsis">
                  ...
                </span>
              ) : (
                <button
                  key={index}
                  className={`his-paginator-page ${
                    currentPage === page ? "active" : ""
                  }`}
                  onClick={() => goToPage(page)}
                >
                  {page}
                </button>
              )
            )}
          </div>

          <button
            className="his-paginator-btn his-paginator-btn-next"
            disabled={currentPage === totalPages}
            onClick={() => goToPage(currentPage + 1)}
          >
            Weiter
            <span className="icon icon-chevron-right"></span>
          </button>
        </div>

        <div className="his-paginator-settings">
          <label className="his-paginator-label">
            Einträge pro Seite:
            <select
              className="his-paginator-select"
              value={itemsPerPage}
              onChange={(e) => changeItemsPerPage(parseInt(e.target.value))}
            >
              <option value="25">25</option>
              <option value="50">50</option>
              <option value="100">100</option>
              <option value="200">200</option>
            </select>
          </label>
        </div>
      </div>
    </div>
  );
};

// ===== CONFIGURATION PANEL =====
const ConfigurationPanel = () => {
  const { showConfigPanel, setShowConfigPanel } = useContext(HISContext);

  return (
    <div className={`his-config-panel ${showConfigPanel ? "open" : ""}`}>
      <div className="his-config-header">
        <h3>Spaltenkonfiguration</h3>
        <button
          onClick={() => setShowConfigPanel(false)}
          style={{
            float: "right",
            background: "none",
            border: "none",
            cursor: "pointer",
          }}
        >
          ✕
        </button>
      </div>
      <div className="his-config-content">
        <div style={{ marginBottom: "20px" }}>
          <h4>Sichtbare Spalten:</h4>
          {[
            "Patient-ID",
            "Name",
            "Geburtsdatum",
            "Alter",
            "Geschlecht",
            "Status",
            "Letzte Begegnung",
          ].map((column) => (
            <label key={column} style={{ display: "block", margin: "10px 0" }}>
              <input type="checkbox" defaultChecked /> {column}
            </label>
          ))}
        </div>
        <div>
          <h4>Anzeigeoptionen:</h4>
          <label style={{ display: "block", margin: "10px 0" }}>
            <input type="checkbox" defaultChecked /> Erweiterte Details
          </label>
          <label style={{ display: "block", margin: "10px 0" }}>
            <input type="checkbox" defaultChecked /> Echtzeitaktualisierung
          </label>
        </div>
      </div>
    </div>
  );
};

// ===== MAIN CONTENT =====
const MainContent = () => {
  const { activeTab, patients } = useContext(HISContext);

  const renderContent = () => {
    switch (activeTab) {
      case "patients":
        return (
          <>
            <div className="his-section-header">
              <div className="his-section-title">
                Patienten
                <span className="his-section-count">({patients.length})</span>
              </div>
              <div className="his-section-actions">
                <button className="his-btn his-btn-secondary">
                  <span className="icon icon-export"></span>
                  Export
                </button>
                <button className="his-btn his-btn-primary">
                  <span className="icon icon-plus"></span>
                  Neuer Patient
                </button>
              </div>
            </div>
            <Toolbar />
            <PatientList />
            <Paginator />
          </>
        );
      default:
        return (
          <div style={{ padding: "40px", textAlign: "center" }}>
            <h2>🚧 {activeTab} - In Entwicklung</h2>
            <p>
              Diese Funktion wird in einer zukünftigen Version verfügbar sein.
            </p>
          </div>
        );
    }
  };

  return <main className="his-content">{renderContent()}</main>;
};

// ===== MAIN APP COMPONENT =====
const App = () => {
  return (
    <div className="his-container">
      <Header />
      <Navigation />
      <MainContent />
      <ConfigurationPanel />
    </div>
  );
};

// ===== APP WITH PROVIDER =====
const AppWithProvider = () => {
  return (
    <HISProvider>
      <App />
    </HISProvider>
  );
};

export default AppWithProvider;
