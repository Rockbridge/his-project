import React, { useState, useEffect, createContext, useContext } from "react";
import "./App.css";

// ===== CONTEXT & STATE MANAGEMENT =====
const HISContext = createContext();

const HISProvider = ({ children }) => {
  const [patients, setPatients] = useState([]);
  const [encounters, setEncounters] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [user, setUser] = useState({ name: "Dr. Mustermann", role: "Arzt" });
  const [currentPage, setCurrentPage] = useState("dashboard");
  const [selectedPatient, setSelectedPatient] = useState(null);

  const contextValue = {
    patients,
    setPatients,
    encounters,
    setEncounters,
    loading,
    setLoading,
    error,
    setError,
    user,
    setUser,
    currentPage,
    setCurrentPage,
    selectedPatient,
    setSelectedPatient,
  };

  return (
    <HISContext.Provider value={contextValue}>{children}</HISContext.Provider>
  );
};

const useHIS = () => {
  const context = useContext(HISContext);
  if (!context) {
    throw new Error("useHIS must be used within a HISProvider");
  }
  return context;
};

// ===== API SERVICE =====
class HISApiService {
  constructor() {
    this.baseUrl = "http://localhost:8080/api/v1";
    this.authHeader = "Basic " + btoa("admin:dev-password");
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const config = {
      headers: {
        "Content-Type": "application/json",
        Authorization: this.authHeader,
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  }

  // Search patients (using as patient list workaround)
  async searchPatients(searchTerm = "", page = 0, size = 50) {
    const params = new URLSearchParams({
      searchTerm: searchTerm,
      page: page.toString(),
      size: size.toString(),
    });
    return this.request(`/patients/search?${params}`);
  }

  async getPatient(patientId) {
    return this.request(`/patients/${patientId}`);
  }

  async getPatientByKVNR(kvnr) {
    return this.request(`/patients/kvnr/${kvnr}`);
  }

  async createPatient(patientData) {
    return this.request("/patients", {
      method: "POST",
      body: JSON.stringify(patientData),
    });
  }

  async getPatientEncounters(patientId, page = 0, size = 20) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    return this.request(`/encounters/patient/${patientId}?${params}`);
  }

  async getSystemHealth() {
    const response = await fetch("http://localhost:8080/actuator/health");
    return response.json();
  }
}

const apiService = new HISApiService();

// ===== UI COMPONENTS =====

// Status Badge Component
const StatusBadge = ({ status, type = "default" }) => {
  const statusClasses = {
    ACTIVE: "his-status-active",
    INACTIVE: "his-status-inactive",
    PLANNED: "his-status-planned",
    IN_PROGRESS: "his-status-progress",
    COMPLETED: "his-status-completed",
    CANCELLED: "his-status-cancelled",
  };

  const statusLabels = {
    ACTIVE: "Aktiv",
    INACTIVE: "Inaktiv",
    PLANNED: "Geplant",
    IN_PROGRESS: "Läuft",
    COMPLETED: "Abgeschlossen",
    CANCELLED: "Abgebrochen",
  };

  return (
    <span
      className={`his-status ${statusClasses[status] || "his-status-default"}`}
    >
      {statusLabels[status] || status}
    </span>
  );
};

// Loading Spinner Component
const LoadingSpinner = ({ message = "Laden..." }) => (
  <div className="his-loading">
    <div className="his-spinner"></div>
    <span className="his-loading-text">{message}</span>
  </div>
);

// Error Message Component
const ErrorMessage = ({ error, onRetry }) => (
  <div className="his-error">
    <span className="his-error-text">⚠️ {error}</span>
    {onRetry && (
      <button className="his-btn his-btn-secondary" onClick={onRetry}>
        Erneut versuchen
      </button>
    )}
  </div>
);

// Header Component
const Header = () => {
  const { user } = useHIS();

  return (
    <header className="his-header">
      <div className="his-logo">🏥 Hospital Information System</div>
      <div className="his-user-info">
        <span className="his-user-name">{user.name}</span>
        <span className="his-user-role">({user.role})</span>
      </div>
    </header>
  );
};

// Navigation Component
const Navigation = () => {
  const { currentPage, setCurrentPage } = useHIS();

  const tabs = [
    { id: "dashboard", label: "📊 Dashboard" },
    { id: "patients", label: "👥 Patienten" },
    { id: "encounters", label: "🩺 Behandlungen" },
    { id: "settings", label: "⚙️ System" },
  ];

  return (
    <nav className="his-nav-tabs">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          className={`his-nav-tab ${currentPage === tab.id ? "active" : ""}`}
          onClick={() => setCurrentPage(tab.id)}
        >
          {tab.label}
        </button>
      ))}
    </nav>
  );
};

// Dashboard Page Component
const Dashboard = () => {
  const { patients, encounters, loading } = useHIS();
  const [systemHealth, setSystemHealth] = useState(null);

  useEffect(() => {
    const fetchHealth = async () => {
      try {
        const health = await apiService.getSystemHealth();
        setSystemHealth(health);
      } catch (error) {
        console.error("Health check failed:", error);
      }
    };

    fetchHealth();
    const interval = setInterval(fetchHealth, 30000);
    return () => clearInterval(interval);
  }, []);

  const stats = {
    totalPatients: patients.length,
    malePatients: patients.filter((p) => p.gender === "MALE").length,
    femalePatients: patients.filter((p) => p.gender === "FEMALE").length,
    activeInsurance: patients.filter((p) => p.insuranceStatus === "ACTIVE")
      .length,
    totalEncounters: encounters.length,
  };

  if (loading) {
    return <LoadingSpinner message="Dashboard wird geladen..." />;
  }

  return (
    <div className="his-dashboard">
      <div className="his-section-header">
        <h2 className="his-section-title">
          📊 Dashboard
          <span className="his-section-count">Systemübersicht</span>
        </h2>
      </div>

      {/* System Health Status */}
      <div className="his-health-status">
        <h3>System Status</h3>
        <div className="his-health-indicators">
          <div
            className={`his-health-indicator ${
              systemHealth?.status === "UP" ? "healthy" : "unhealthy"
            }`}
          >
            <span className="his-health-dot"></span>
            System: {systemHealth?.status || "UNKNOWN"}
          </div>
          {systemHealth?.components?.db && (
            <div
              className={`his-health-indicator ${
                systemHealth.components.db.status === "UP"
                  ? "healthy"
                  : "unhealthy"
              }`}
            >
              <span className="his-health-dot"></span>
              Datenbank: {systemHealth.components.db.status}
            </div>
          )}
        </div>
      </div>

      {/* Statistics Cards */}
      <div className="his-stats-grid">
        <div className="his-stat-card">
          <div className="his-stat-number">{stats.totalPatients}</div>
          <div className="his-stat-label">Patienten gesamt</div>
        </div>
        <div className="his-stat-card">
          <div className="his-stat-number">{stats.malePatients}</div>
          <div className="his-stat-label">♂️ Männlich</div>
        </div>
        <div className="his-stat-card">
          <div className="his-stat-number">{stats.femalePatients}</div>
          <div className="his-stat-label">♀️ Weiblich</div>
        </div>
        <div className="his-stat-card">
          <div className="his-stat-number">{stats.activeInsurance}</div>
          <div className="his-stat-label">Aktive Versicherung</div>
        </div>
      </div>
    </div>
  );
};

// Patients Page Component
const PatientsPage = () => {
  const {
    patients,
    setPatients,
    loading,
    setLoading,
    error,
    setError,
    setSelectedPatient,
    setCurrentPage,
  } = useHIS();
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    loadPatients();
  }, []);

  const loadPatients = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiService.searchPatients("", 0, 50);
      setPatients(data.content || []);
    } catch (error) {
      setError("Fehler beim Laden der Patienten: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiService.searchPatients(searchTerm, 0, 50);
      setPatients(data.content || []);
    } catch (error) {
      setError("Fehler bei der Suche: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handlePatientClick = async (patient) => {
    try {
      const details = await apiService.getPatient(patient.id);
      setSelectedPatient(details);
      setCurrentPage("patient-detail");
    } catch (error) {
      setError("Fehler beim Laden der Patientendetails: " + error.message);
    }
  };

  const formatGender = (gender) => {
    switch (gender) {
      case "MALE":
        return <span className="his-gender">M</span>;
      case "FEMALE":
        return <span className="his-gender">W</span>;
      case "OTHER":
        return <span className="his-gender">D</span>;
      default:
        return <span className="his-gender">U</span>;
    }
  };

  return (
    <div className="his-patients-page">
      <div className="his-section-header">
        <h2 className="his-section-title">
          👥 Patienten
          <span className="his-section-count">({patients.length})</span>
        </h2>
        <div className="his-section-actions">
          <button className="his-btn his-btn-primary" onClick={loadPatients}>
            ↻ Aktualisieren
          </button>
        </div>
      </div>

      {error && <ErrorMessage error={error} onRetry={loadPatients} />}

      <div className="his-toolbar">
        <div className="his-search-container">
          <input
            type="text"
            className="his-search-input"
            placeholder="Patient suchen (Name, KVNR)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyPress={(e) => e.key === "Enter" && handleSearch()}
          />
          <button className="his-btn his-btn-secondary" onClick={handleSearch}>
            🔍 Suchen
          </button>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner message="Patienten werden geladen..." />
      ) : (
        <div className="his-list-container">
          <table className="his-list">
            <thead>
              <tr>
                <th>Name</th>
                <th>KVNR</th>
                <th>Geburtsdatum</th>
                <th>Geschlecht</th>
                <th>Versicherung</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {patients.map((patient) => (
                <tr
                  key={patient.id}
                  className="his-list-row"
                  onClick={() => handlePatientClick(patient)}
                >
                  <td className="his-cell-primary">{patient.fullName}</td>
                  <td>
                    <code className="his-kvnr">{patient.kvnr}</code>
                  </td>
                  <td>{patient.birthDate}</td>
                  <td>{formatGender(patient.gender)}</td>
                  <td>{patient.insuranceCompanyName || "-"}</td>
                  <td>
                    <StatusBadge status={patient.insuranceStatus} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

// Patient Detail Component
const PatientDetail = () => {
  const { selectedPatient, setCurrentPage } = useHIS();
  const [encounters, setEncounters] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (selectedPatient?.id) {
      loadEncounters();
    }
  }, [selectedPatient]);

  const loadEncounters = async () => {
    setLoading(true);
    try {
      const data = await apiService.getPatientEncounters(selectedPatient.id);
      setEncounters(data.content || []);
    } catch (error) {
      console.error("Error loading encounters:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleBack = () => {
    setCurrentPage("patients");
  };

  if (!selectedPatient) {
    return <div>Kein Patient ausgewählt</div>;
  }

  const formatGender = (gender) => {
    switch (gender) {
      case "MALE":
        return "♂️ Männlich";
      case "FEMALE":
        return "♀️ Weiblich";
      case "OTHER":
        return "⚤ Divers";
      default:
        return "Unbekannt";
    }
  };

  const formatInsuranceType = (type) => {
    switch (type) {
      case "STATUTORY":
        return "Gesetzlich";
      case "PRIVATE":
        return "Privat";
      case "SELF_PAYER":
        return "Selbstzahler";
      default:
        return "Andere";
    }
  };

  return (
    <div className="his-patient-detail">
      <div className="his-section-header">
        <h2 className="his-section-title">
          👤 Patient Details
          <span className="his-section-count">{selectedPatient.fullName}</span>
        </h2>
        <div className="his-section-actions">
          <button className="his-btn his-btn-secondary" onClick={handleBack}>
            ← Zurück zur Liste
          </button>
        </div>
      </div>

      <div className="his-detail-grid">
        <div className="his-detail-card">
          <h3>Persönliche Daten</h3>
          <div className="his-detail-row">
            <label>Name:</label>
            <span>{selectedPatient.fullName}</span>
          </div>
          <div className="his-detail-row">
            <label>KVNR:</label>
            <span>
              <code className="his-kvnr">{selectedPatient.kvnr}</code>
            </span>
          </div>
          <div className="his-detail-row">
            <label>Geburtsdatum:</label>
            <span>{selectedPatient.birthDate}</span>
          </div>
          <div className="his-detail-row">
            <label>Geschlecht:</label>
            <span>{formatGender(selectedPatient.gender)}</span>
          </div>
          {selectedPatient.phone && (
            <div className="his-detail-row">
              <label>Telefon:</label>
              <span>{selectedPatient.phone}</span>
            </div>
          )}
          {selectedPatient.email && (
            <div className="his-detail-row">
              <label>E-Mail:</label>
              <span>{selectedPatient.email}</span>
            </div>
          )}
        </div>

        <div className="his-detail-card">
          <h3>Versicherungsdaten</h3>
          <div className="his-detail-row">
            <label>Versicherungstyp:</label>
            <span>{formatInsuranceType(selectedPatient.insuranceType)}</span>
          </div>
          <div className="his-detail-row">
            <label>Krankenkasse:</label>
            <span>
              {selectedPatient.insuranceCompanyName || "Nicht angegeben"}
            </span>
          </div>
          {selectedPatient.insuranceNumber && (
            <div className="his-detail-row">
              <label>Versicherungsnummer:</label>
              <span>{selectedPatient.insuranceNumber}</span>
            </div>
          )}
          <div className="his-detail-row">
            <label>Status:</label>
            <span>
              <StatusBadge status={selectedPatient.insuranceStatus} />
            </span>
          </div>
        </div>
      </div>

      <div className="his-detail-card">
        <h3>Behandlungen ({encounters.length})</h3>
        {loading ? (
          <LoadingSpinner message="Behandlungen werden geladen..." />
        ) : encounters.length > 0 ? (
          <div className="his-list-container">
            <table className="his-list">
              <thead>
                <tr>
                  <th>Datum</th>
                  <th>Typ</th>
                  <th>Status</th>
                  <th>Abrechnungskontext</th>
                </tr>
              </thead>
              <tbody>
                {encounters.map((encounter) => (
                  <tr key={encounter.id} className="his-list-row">
                    <td>
                      {new Date(encounter.encounterDate).toLocaleDateString(
                        "de-DE"
                      )}
                    </td>
                    <td>{encounter.type}</td>
                    <td>
                      <StatusBadge status={encounter.status} />
                    </td>
                    <td>{encounter.billingContext}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="his-empty-state">Keine Behandlungen vorhanden</p>
        )}
      </div>
    </div>
  );
};

// Settings Page Component
const SettingsPage = () => {
  const [systemHealth, setSystemHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSystemHealth();
  }, []);

  const fetchSystemHealth = async () => {
    setLoading(true);
    try {
      const health = await apiService.getSystemHealth();
      setSystemHealth(health);
    } catch (error) {
      console.error("Health check failed:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="his-settings-page">
      <div className="his-section-header">
        <h2 className="his-section-title">
          ⚙️ System
          <span className="his-section-count">Status & Einstellungen</span>
        </h2>
        <div className="his-section-actions">
          <button
            className="his-btn his-btn-primary"
            onClick={fetchSystemHealth}
          >
            ↻ Aktualisieren
          </button>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner message="Systemstatus wird geladen..." />
      ) : (
        <div className="his-settings-grid">
          <div className="his-detail-card">
            <h3>System Health</h3>
            <div className="his-health-status">
              <div
                className={`his-health-indicator ${
                  systemHealth?.status === "UP" ? "healthy" : "unhealthy"
                }`}
              >
                <span className="his-health-dot"></span>
                Gesamtstatus: {systemHealth?.status || "UNKNOWN"}
              </div>

              {systemHealth?.components &&
                Object.entries(systemHealth.components).map(
                  ([name, component]) => (
                    <div
                      key={name}
                      className={`his-health-indicator ${
                        component.status === "UP" ? "healthy" : "unhealthy"
                      }`}
                    >
                      <span className="his-health-dot"></span>
                      {name}: {component.status}
                    </div>
                  )
                )}
            </div>
          </div>

          <div className="his-detail-card">
            <h3>API Endpunkte</h3>
            <div className="his-api-endpoints">
              <div className="his-endpoint">
                <strong>API Gateway:</strong> http://localhost:8080
              </div>
              <div className="his-endpoint">
                <strong>Patient Service:</strong> http://localhost:8081
              </div>
              <div className="his-endpoint">
                <strong>Encounter Service:</strong> http://localhost:8082
              </div>
            </div>
          </div>

          <div className="his-detail-card">
            <h3>Systeminfos</h3>
            <div className="his-detail-row">
              <label>Frontend Version:</label>
              <span>1.0.0</span>
            </div>
            <div className="his-detail-row">
              <label>Backend API:</label>
              <span>v1</span>
            </div>
            <div className="his-detail-row">
              <label>Letzte Aktualisierung:</label>
              <span>{new Date().toLocaleString("de-DE")}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// Main Layout Component
const Layout = ({ children }) => (
  <div className="his-container">
    <Header />
    <Navigation />
    <main className="his-main">
      <div className="his-content">{children}</div>
    </main>
  </div>
);

// Main App Component
const HISApp = () => {
  const { currentPage, patients, setPatients, setLoading, setError } = useHIS();

  // Load initial data
  useEffect(() => {
    const loadInitialData = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await apiService.searchPatients("", 0, 50);
        setPatients(data.content || []);
      } catch (error) {
        setError("Fehler beim Laden der Anfangsdaten: " + error.message);
      } finally {
        setLoading(false);
      }
    };

    loadInitialData();
  }, [setPatients, setLoading, setError]);

  const renderPage = () => {
    switch (currentPage) {
      case "dashboard":
        return <Dashboard />;
      case "patients":
        return <PatientsPage />;
      case "patient-detail":
        return <PatientDetail />;
      case "settings":
        return <SettingsPage />;
      default:
        return <Dashboard />;
    }
  };

  return <Layout>{renderPage()}</Layout>;
};

// Root App Component
const App = () => {
  return (
    <HISProvider>
      <HISApp />
    </HISProvider>
  );
};

export default App;
