import React, { useState, useEffect, createContext, useContext } from "react";
import "./App.css";

// ===== CONTEXT & STATE MANAGEMENT =====
const HISContext = createContext();

const HISProvider = ({ children }) => {
  const [patients, setPatients] = useState([]);
  const [encounters, _setEncounters] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [selectedEncounters, setSelectedEncounters] = useState([]);
  const [expandedRows, setExpandedRows] = useState(new Set());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [user, setUser] = useState({ name: "Dr. Schmidt", role: "Physician" });
  const [activeTab, setActiveTab] = useState("stationsübersicht");
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(18);
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

  // Load Patient Encounters
  const loadPatientEncounters = async (patientId) => {
    console.log(`🔍 Loading encounters for patient: ${patientId}`);

    try {
      const data = await apiRequest(
        `/encounters/patient/${patientId}?page=0&size=10`
      );
      console.log(`✅ Encounter API Response for ${patientId}:`, data);

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
      return [];
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

          let firstName = patient.firstName || "";
          let lastName = patient.lastName || "";
          let patientName = patient.fullName;

          if (!patientName && (firstName || lastName)) {
            patientName = lastName ? `${lastName}, ${firstName}` : firstName;
          }

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
            encounters: encounters,
            status: encounters.some((e) => e.status === "IN_PROGRESS")
              ? "in_progress"
              : "active",
            room: `Z.${Math.floor(Math.random() * 30) + 100}`,
          };
        })
      );

      console.log(`✅ Total enhanced patients: ${enhancedPatients.length}`);
      setPatients(enhancedPatients);
    } catch (err) {
      console.error("❌ Error loading patients:", err);
      setError(`Fehler beim Laden der Patienten: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const calculateAge = (birthDate) => {
    const birth = new Date(birthDate);
    const today = new Date();
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

  const toggleRow = (index) => {
    const newExpandedRows = new Set(expandedRows);
    if (newExpandedRows.has(index)) {
      newExpandedRows.delete(index);
    } else {
      newExpandedRows.add(index);
    }
    setExpandedRows(newExpandedRows);
  };

  const filteredPatients = patients.filter((patient) => {
    const searchLower = searchTerm.toLowerCase();
    return (
      !searchTerm ||
      patient.patientName?.toLowerCase().includes(searchLower) ||
      patient.kvnr?.toLowerCase().includes(searchLower) ||
      patient.room?.toLowerCase().includes(searchLower)
    );
  });

  useEffect(() => {
    // Lade Patienten beim Start der App
    loadPatients();

    const interval = setInterval(() => {
      setCurrentDateTime(new Date());
    }, 60000);
    return () => clearInterval(interval);
  }, []);

  return (
    <HISContext.Provider
      value={{
        patients: filteredPatients,
        encounters,
        selectedPatient,
        setSelectedPatient,
        selectedEncounters,
        setSelectedEncounters,
        expandedRows,
        toggleRow,
        loading,
        error,
        user,
        setUser,
        activeTab,
        setActiveTab,
        searchTerm,
        setSearchTerm,
        currentPage,
        setCurrentPage,
        itemsPerPage,
        setItemsPerPage,
        showConfigPanel,
        setShowConfigPanel,
        currentDateTime,
        loadPatients,
      }}
    >
      {children}
    </HISContext.Provider>
  );
};

// ===== HEADER COMPONENT =====
const Header = () => {
  const { user, currentDateTime } = useContext(HISContext);

  return (
    <header className="his-header">
      <div className="his-logo">Hospital Information System</div>
      <div className="his-header-info">
        <div className="his-datetime">
          {currentDateTime.toLocaleDateString("de-DE", {
            weekday: "long",
            year: "numeric",
            month: "long",
            day: "numeric",
          })}{" "}
          -{" "}
          {currentDateTime.toLocaleTimeString("de-DE", {
            hour: "2-digit",
            minute: "2-digit",
          })}
        </div>
        <div className="his-user-info">
          <span className="his-user-name">{user.name}</span>
          <span className="his-user-role">{user.role}</span>
        </div>
      </div>
    </header>
  );
};

// ===== NAVIGATION COMPONENT =====
const Navigation = () => {
  const { activeTab, setActiveTab } = useContext(HISContext);

  const tabs = [
    { id: "dashboard", label: "Dashboard", icon: "📊" },
    { id: "stationsübersicht", label: "Stationsübersicht", icon: "👥" },
    { id: "behandlungen", label: "Behandlungen", icon: "🩺" },
    { id: "berichte", label: "Berichte", icon: "📋" },
    { id: "einstellungen", label: "Einstellungen", icon: "⚙️" },
  ];

  return (
    <nav className="his-nav-tabs">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          className={`his-nav-tab ${activeTab === tab.id ? "active" : ""}`}
          onClick={() => setActiveTab(tab.id)}
        >
          <span className="tab-icon">{tab.icon}</span>
          {tab.label}
        </button>
      ))}
    </nav>
  );
};

// ===== TOOLBAR COMPONENT =====
const Toolbar = () => {
  const {
    searchTerm,
    setSearchTerm,
    setShowConfigPanel,
    loadPatients,
    _patients,
  } = useContext(HISContext);

  return (
    <div className="his-toolbar">
      <div className="his-toolbar-left">
        <input
          type="text"
          placeholder="Suche nach Name, KVNR oder Zimmer..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="his-search-input"
        />
        <button className="his-btn his-btn-primary">
          <span className="icon icon-plus">➕</span>
          Neuer Patient
        </button>
      </div>
      <div className="his-toolbar-right">
        <button className="his-btn his-btn-secondary">
          <span className="icon icon-export">📤</span>
          Export
        </button>
        <button className="his-btn his-btn-secondary" onClick={loadPatients}>
          <span className="icon icon-refresh">🔄</span>
          Aktualisieren
        </button>
        <button
          className="his-btn his-btn-secondary"
          onClick={() => setShowConfigPanel(true)}
        >
          <span className="icon icon-settings">⚙️</span>
          Spalten
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

  const formatDateTime = (dateString) => {
    if (!dateString) return "-";
    return new Date(dateString).toLocaleString("de-DE", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
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

  const getEncounterTypeText = (type) => {
    const typeMap = {
      INITIAL: "Erstbehandlung",
      CONSULTATION: "Konsultation",
      EMERGENCY: "Notfall",
      ROUTINE_CHECKUP: "Routineuntersuchung",
      FOLLOW_UP: "Nachkontrolle",
      SURGERY: "Operation",
      DIAGNOSTIC: "Diagnostik",
      OUTPATIENT: "Ambulant",
      INPATIENT: "Stationär",
    };
    return typeMap[type] || type;
  };

  const getEncounterStatusText = (status) => {
    const statusMap = {
      PLANNED: "Geplant",
      IN_PROGRESS: "Laufend",
      COMPLETED: "Abgeschlossen",
      CANCELLED: "Storniert",
      NO_SHOW: "Nicht erschienen",
      POSTPONED: "Verschoben",
    };
    return statusMap[status] || status;
  };

  const getBillingContextText = (context) => {
    const contextMap = {
      GKV: "Gesetzliche KV",
      PKV: "Private KV",
      SELF_PAY: "Selbstzahler",
      BG: "Berufsgenossenschaft",
      INSURANCE: "Versicherung",
    };
    return contextMap[context] || context;
  };

  const getSOAPSectionText = (section) => {
    const sectionMap = {
      SUBJECTIVE: "Subjektiv",
      OBJECTIVE: "Objektiv",
      ASSESSMENT: "Beurteilung",
      PLAN: "Plan",
    };
    return sectionMap[section] || section;
  };

  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = Math.min(startIndex + itemsPerPage, patients.length);
  const pagePatients = patients.slice(startIndex, endIndex);

  if (loading) {
    return (
      <>
        <Toolbar />
        <div className="his-list-container">
          <table className="his-list">
            <thead>
              <tr>
                <th className="his-icon-column"></th>
                <th style={{ width: "16%" }}>Patient-ID</th>
                <th style={{ width: "22%" }}>Name</th>
                <th style={{ width: "12%" }}>Geburtsdatum</th>
                <th style={{ width: "10%" }}>Alter</th>
                <th style={{ width: "8%" }}>Geschlecht</th>
                <th style={{ width: "12%" }}>Status</th>
                <th style={{ width: "20%" }}>Letzte Begegnung</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td colSpan="8" className="his-loading-cell">
                  <div className="his-loading">
                    <div className="loading-spinner"></div>
                    <p>Lade Patienten...</p>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <Paginator />
      </>
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
              <th
                style={{
                  width: "36px",
                  textAlign: "center",
                  padding: "8px 10px",
                }}
              ></th>
              <th style={{ width: "10%" }}>Patient-ID</th>
              <th style={{ width: "24%" }}>Name</th>
              <th style={{ width: "8%" }}>Geburtsdatum</th>
              <th style={{ width: "10%" }}>Alter</th>
              <th style={{ width: "8%" }}>Geschlecht</th>
              <th style={{ width: "6%" }}>Status</th>
              <th style={{ width: "34%" }}>Letzte Begegnung</th>
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
                      <div
                        className={`his-expand-icon ${
                          isExpanded ? "expanded" : "collapsed"
                        }`}
                      ></div>
                    </td>
                    <td>
                      <code>{patient.id?.substring(0, 8) || "N/A"}</code>
                    </td>
                    <td className="his-patient-info">
                      <strong>{patient.patientName}</strong>
                      <span>KVNR: {patient.kvnr}</span>
                    </td>
                    <td className="his-admission-date">
                      {formatDate(patient.birthDate)}
                    </td>
                    <td>{patient.age} Jahre</td>
                    <td>{getGenderText(patient.gender)}</td>
                    <td className="his-patient-status">
                      <span className={`his-status-badge ${patient.status}`}>
                        {getStatusText(patient.status)}
                      </span>
                    </td>
                    <td className="his-admission-date">
                      {formatDate(patient.lastEncounter)}
                    </td>
                  </tr>
                  {isExpanded && (
                    <tr className="his-expanded-content">
                      <td colSpan="8">
                        <div className="his-patient-details">
                          <div className="his-details-sections">
                            {/* Kontaktdaten Sektion */}
                            <div className="his-detail-section his-contact-section">
                              <h4>Kontaktdaten</h4>
                              <div className="his-contact-grid">
                                <div className="his-contact-item">
                                  <span className="his-contact-label">
                                    Telefon:
                                  </span>
                                  <span className="his-contact-value">
                                    {patient.phone}
                                  </span>
                                </div>
                                <div className="his-contact-item">
                                  <span className="his-contact-label">
                                    E-Mail:
                                  </span>
                                  <span className="his-contact-value">
                                    {patient.email}
                                  </span>
                                </div>
                                <div className="his-contact-item">
                                  <span className="his-contact-label">
                                    Zimmer:
                                  </span>
                                  <span className="his-contact-value">
                                    {patient.room}
                                  </span>
                                </div>
                                <div className="his-contact-item">
                                  <span className="his-contact-label">
                                    KVNR:
                                  </span>
                                  <span className="his-contact-value">
                                    {patient.kvnr}
                                  </span>
                                </div>
                              </div>
                            </div>

                            {/* Encounter Details Sektion */}
                            <div className="his-detail-section his-encounter-section">
                              <h4>
                                Behandlungshistorie (
                                {patient.encounters?.length || 0} Einträge)
                              </h4>
                              {patient.encounters &&
                              patient.encounters.length > 0 ? (
                                <div className="his-encounter-list">
                                  {patient.encounters.map((encounter, idx) => (
                                    <div
                                      key={encounter.id || idx}
                                      className="his-encounter-row"
                                    >
                                      <div className="his-encounter-main">
                                        <div className="his-encounter-date">
                                          <strong>
                                            {formatDate(
                                              encounter.encounterDate
                                            )}
                                          </strong>
                                          <span className="his-encounter-time">
                                            {encounter.encounterDate
                                              ? new Date(
                                                  encounter.encounterDate
                                                ).toLocaleTimeString("de-DE", {
                                                  hour: "2-digit",
                                                  minute: "2-digit",
                                                })
                                              : "--:--"}
                                          </span>
                                        </div>
                                        <div className="his-encounter-info">
                                          <div className="his-encounter-type">
                                            {getEncounterTypeText(
                                              encounter.encounterType ||
                                                encounter.type ||
                                                "UNKNOWN"
                                            )}
                                          </div>
                                          <div className="his-encounter-status">
                                            <span
                                              className={`his-encounter-status-badge ${
                                                encounter.status?.toLowerCase() ||
                                                "unknown"
                                              }`}
                                            >
                                              {getEncounterStatusText(
                                                encounter.status || "UNBEKANNT"
                                              )}
                                            </span>
                                          </div>
                                        </div>
                                      </div>
                                      <div className="his-encounter-details">
                                        {encounter.department && (
                                          <div className="his-encounter-detail">
                                            <span className="his-detail-label">
                                              Abteilung:
                                            </span>
                                            <span className="his-detail-value">
                                              {encounter.department}
                                            </span>
                                          </div>
                                        )}
                                        {encounter.practitionerId && (
                                          <div className="his-encounter-detail">
                                            <span className="his-detail-label">
                                              Arzt-ID:
                                            </span>
                                            <span className="his-detail-value">
                                              {encounter.practitionerId.substring(
                                                0,
                                                8
                                              )}
                                              ...
                                            </span>
                                          </div>
                                        )}
                                        {encounter.reason && (
                                          <div className="his-encounter-detail">
                                            <span className="his-detail-label">
                                              Grund:
                                            </span>
                                            <span className="his-detail-value">
                                              {encounter.reason}
                                            </span>
                                          </div>
                                        )}
                                        {encounter.billingContext && (
                                          <div className="his-encounter-detail">
                                            <span className="his-detail-label">
                                              Abrechnung:
                                            </span>
                                            <span className="his-detail-value">
                                              {getBillingContextText(
                                                encounter.billingContext
                                              )}
                                            </span>
                                          </div>
                                        )}
                                        {/* Audit-Felder */}
                                        <div className="his-encounter-audit">
                                          {encounter.createdAt && (
                                            <div className="his-encounter-detail">
                                              <span className="his-detail-label">
                                                Erstellt:
                                              </span>
                                              <span className="his-detail-value">
                                                {formatDateTime(
                                                  encounter.createdAt
                                                )}
                                              </span>
                                            </div>
                                          )}
                                          {encounter.updatedAt &&
                                            encounter.updatedAt !==
                                              encounter.createdAt && (
                                              <div className="his-encounter-detail">
                                                <span className="his-detail-label">
                                                  Aktualisiert:
                                                </span>
                                                <span className="his-detail-value">
                                                  {formatDateTime(
                                                    encounter.updatedAt
                                                  )}
                                                </span>
                                              </div>
                                            )}
                                        </div>
                                        {/* SOAP Dokumentation */}
                                        {encounter.documentation &&
                                          encounter.documentation.length >
                                            0 && (
                                            <div className="his-soap-documentation">
                                              <div className="his-soap-header">
                                                <span className="his-detail-label">
                                                  SOAP-Dokumentation:
                                                </span>
                                                <span className="his-soap-count">
                                                  (
                                                  {
                                                    encounter.documentation
                                                      .length
                                                  }{" "}
                                                  Einträge)
                                                </span>
                                              </div>
                                              <div className="his-soap-entries">
                                                {encounter.documentation.map(
                                                  (doc, docIdx) => (
                                                    <div
                                                      key={docIdx}
                                                      className="his-soap-entry"
                                                    >
                                                      <strong className="his-soap-section">
                                                        {getSOAPSectionText(
                                                          doc.soapSection
                                                        )}
                                                        :
                                                      </strong>
                                                      <span className="his-soap-content">
                                                        {doc.content?.substring(
                                                          0,
                                                          100
                                                        )}
                                                        {doc.content?.length >
                                                        100
                                                          ? "..."
                                                          : ""}
                                                      </span>
                                                      {doc.authorId && (
                                                        <div className="his-soap-author">
                                                          <span className="his-detail-label">
                                                            Autor:
                                                          </span>
                                                          <span className="his-detail-value">
                                                            {doc.authorId.substring(
                                                              0,
                                                              8
                                                            )}
                                                            ...
                                                          </span>
                                                        </div>
                                                      )}
                                                      {doc.createdAt && (
                                                        <div className="his-soap-created">
                                                          <span className="his-detail-label">
                                                            Erstellt:
                                                          </span>
                                                          <span className="his-detail-value">
                                                            {formatDateTime(
                                                              doc.createdAt
                                                            )}
                                                          </span>
                                                        </div>
                                                      )}
                                                    </div>
                                                  )
                                                )}
                                              </div>
                                            </div>
                                          )}
                                      </div>
                                    </div>
                                  ))}
                                </div>
                              ) : (
                                <div className="his-no-encounters">
                                  Keine Behandlungen gefunden
                                </div>
                              )}
                            </div>

                            {/* Aktionen Sektion */}
                            <div className="his-detail-section his-actions-section">
                              <h4>Aktionen</h4>
                              <div className="his-patient-actions">
                                <button className="his-btn his-btn-sm his-btn-primary">
                                  📄 Vollständige Details
                                </button>
                                <button className="his-btn his-btn-sm his-btn-secondary">
                                  🩺 Neue Behandlung
                                </button>
                                <button className="his-btn his-btn-sm his-btn-secondary">
                                  📋 Behandlungsplan
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              );
            })}
          </tbody>
        </table>

        {pagePatients.length === 0 && !loading && (
          <div className="his-no-data">
            <p>Keine Patienten gefunden</p>
          </div>
        )}
      </div>
    </>
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

  const getPageNumbers = () => {
    const pages = [];
    const maxVisible = 5;

    if (totalPages <= maxVisible) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage <= 3) {
        for (let i = 1; i <= 4; i++) {
          pages.push(i);
        }
        pages.push("...");
        pages.push(totalPages);
      } else if (currentPage >= totalPages - 2) {
        pages.push(1);
        pages.push("...");
        for (let i = totalPages - 3; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        pages.push(1);
        pages.push("...");
        for (let i = currentPage - 1; i <= currentPage + 1; i++) {
          pages.push(i);
        }
        pages.push("...");
        pages.push(totalPages);
      }
    }

    return pages;
  };

  if (totalPages <= 1) return null;

  return (
    <div className="his-paginator">
      <div className="his-pagination-info">
        Zeige {startItem} bis {endItem} von {patients.length} Einträgen
      </div>
      <div className="his-pagination-controls">
        <select
          value={itemsPerPage}
          onChange={(e) => setItemsPerPage(Number(e.target.value))}
          className="his-items-per-page"
        >
          <option value={10}>10 pro Seite</option>
          <option value={18}>18 pro Seite</option>
          <option value={25}>25 pro Seite</option>
          <option value={50}>50 pro Seite</option>
          <option value={100}>100 pro Seite</option>
        </select>

        <button
          onClick={() => goToPage(currentPage - 1)}
          disabled={currentPage === 1}
          className="his-pagination-btn"
        >
          ‹ Zurück
        </button>

        {getPageNumbers().map((page, index) => (
          <button
            key={index}
            onClick={() => (typeof page === "number" ? goToPage(page) : null)}
            className={`his-pagination-btn ${
              page === currentPage ? "active" : ""
            } ${typeof page !== "number" ? "ellipsis" : ""}`}
            disabled={typeof page !== "number"}
          >
            {page}
          </button>
        ))}

        <button
          onClick={() => goToPage(currentPage + 1)}
          disabled={currentPage === totalPages}
          className="his-pagination-btn"
        >
          Weiter ›
        </button>
      </div>
    </div>
  );
};

// ===== CONFIGURATION PANEL =====
const ConfigurationPanel = () => {
  const { showConfigPanel, setShowConfigPanel } = useContext(HISContext);

  if (!showConfigPanel) return null;

  return (
    <div className={`his-config-panel ${showConfigPanel ? "open" : ""}`}>
      <div className="his-config-header">
        <h3>Spaltenkonfiguration</h3>
        <button
          onClick={() => setShowConfigPanel(false)}
          className="his-config-close"
        >
          ✕
        </button>
      </div>
      <div className="his-config-content">
        <div className="his-config-section">
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
            <label key={column} className="his-config-option">
              <input type="checkbox" defaultChecked /> {column}
            </label>
          ))}
        </div>
        <div className="his-config-section">
          <h4>Anzeigeoptionen:</h4>
          <label className="his-config-option">
            <input type="checkbox" defaultChecked /> Erweiterte Details
          </label>
          <label className="his-config-option">
            <input type="checkbox" defaultChecked /> Echtzeitaktualisierung
          </label>
        </div>
      </div>
    </div>
  );
};

// ===== MAIN CONTENT COMPONENT =====
const MainContent = () => {
  const { activeTab, patients, loading } = useContext(HISContext);

  const renderContent = () => {
    switch (activeTab) {
      case "dashboard":
        return (
          <div className="his-dashboard">
            <div className="his-dashboard-grid">
              <div className="his-dashboard-card">
                <h3>Aktuelle Patienten</h3>
                <div className="his-dashboard-metric">{patients.length}</div>
                <p>Patienten auf der Station</p>
              </div>
              <div className="his-dashboard-card">
                <h3>In Behandlung</h3>
                <div className="his-dashboard-metric">
                  {patients.filter((p) => p.status === "in_progress").length}
                </div>
                <p>Aktive Behandlungen</p>
              </div>
              <div className="his-dashboard-card">
                <h3>Heute aufgenommen</h3>
                <div className="his-dashboard-metric">7</div>
                <p>Neue Aufnahmen heute</p>
              </div>
              <div className="his-dashboard-card">
                <h3>Belegungsrate</h3>
                <div className="his-dashboard-metric">85%</div>
                <p>Station ausgelastet</p>
              </div>
            </div>
          </div>
        );
      case "stationsübersicht":
        return (
          <div className="his-results-list">
            {loading && (
              <div className="his-modal-overlay">
                <div className="his-modal-dialog">
                  <div className="his-modal-content">
                    <div className="his-modal-header">
                      <h3>Daten werden geladen</h3>
                    </div>
                    <div className="his-modal-body">
                      <div className="his-loading">
                        <div className="loading-spinner"></div>
                        <p>Patientendaten werden aktualisiert...</p>
                        <div className="his-loading-progress">
                          <div className="his-progress-bar"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

              <Toolbar />

              <PatientList />

              <Paginator />
          </div>
        );
      default:
        return (
          <div className="his-placeholder">
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

// ===== FOOTER COMPONENT =====
const Footer = () => {
  return (
    <footer className="his-footer">
      <div className="his-footer-info">
        <span className="his-footer-status">🟢 Echtzeitdaten aktiviert</span>
      </div>
      <div className="his-footer-controls">
        {/* Leer für zukünftige Controls */}
      </div>
    </footer>
  );
};

// ===== MAIN APP COMPONENT =====
const App = () => {
  return (
    <div className="his-container">
      <div className="his-header-navigation">
        <Header />
        <Navigation />
      </div>
      <MainContent />
      <Footer />
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
