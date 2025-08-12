import "./App.css";
import React, { useState, useEffect } from "react";

// API Service Klasse
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

  // ✅ Patient Search (funktioniert als Patient List)
  async searchPatients(searchTerm = "", page = 0, size = 20) {
    const params = new URLSearchParams({
      searchTerm: searchTerm,
      page: page.toString(),
      size: size.toString(),
    });
    return this.request(`/patients/search?${params}`);
  }

  // ✅ Patient Details
  async getPatient(patientId) {
    return this.request(`/patients/${patientId}`);
  }

  // ✅ KVNR Lookup
  async getPatientByKVNR(kvnr) {
    return this.request(`/patients/kvnr/${kvnr}`);
  }

  // ✅ Patient Create
  async createPatient(patientData) {
    return this.request("/patients", {
      method: "POST",
      body: JSON.stringify(patientData),
    });
  }

  // ✅ System Health
  async getSystemHealth() {
    const response = await fetch("http://localhost:8080/actuator/health");
    return response.json();
  }
}

const api = new HISApiService();

// Patient List Component
const PatientList = ({ patients, onSelectPatient, loading }) => {
  if (loading) {
    return (
      <div className="flex justify-center items-center p-8">
        <div className="text-blue-600">Lade Patienten...</div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="px-6 py-4 border-b border-gray-200">
        <h3 className="text-lg font-medium text-gray-900">
          Patienten ({patients.length})
        </h3>
      </div>
      <div className="divide-y divide-gray-200">
        {patients.map((patient) => (
          <div
            key={patient.id}
            onClick={() => onSelectPatient(patient)}
            className="px-6 py-4 hover:bg-gray-50 cursor-pointer"
          >
            <div className="flex justify-between items-center">
              <div>
                <div className="text-sm font-medium text-gray-900">
                  {patient.fullName}
                </div>
                <div className="text-sm text-gray-500">
                  KVNR: {patient.kvnr} •{" "}
                  {patient.gender === "MALE"
                    ? "♂️"
                    : patient.gender === "FEMALE"
                    ? "♀️"
                    : "⚤"}
                </div>
                <div className="text-xs text-gray-400">
                  Geb: {patient.birthDate} •{" "}
                  {patient.insuranceCompanyName || "Keine Versicherung"}
                </div>
              </div>
              <div
                className={`px-2 py-1 text-xs rounded-full ${
                  patient.insuranceStatus === "ACTIVE"
                    ? "bg-green-100 text-green-800"
                    : "bg-red-100 text-red-800"
                }`}
              >
                {patient.insuranceStatus}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

// Patient Details Component
const PatientDetail = ({ patient, onBack }) => {
  if (!patient) return null;

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <h3 className="text-lg font-medium text-gray-900">Patient Details</h3>
        <button
          onClick={onBack}
          className="px-4 py-2 text-sm bg-gray-500 text-white rounded hover:bg-gray-600"
        >
          Zurück
        </button>
      </div>
      <div className="px-6 py-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="text-sm font-medium text-gray-700">Name</label>
            <p className="text-sm text-gray-900">{patient.fullName}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">KVNR</label>
            <p className="text-sm text-gray-900 font-mono">{patient.kvnr}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">
              Geburtsdatum
            </label>
            <p className="text-sm text-gray-900">{patient.birthDate}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">
              Geschlecht
            </label>
            <p className="text-sm text-gray-900">
              {patient.gender === "MALE"
                ? "♂️ Männlich"
                : patient.gender === "FEMALE"
                ? "♀️ Weiblich"
                : patient.gender === "OTHER"
                ? "⚤ Divers"
                : "Unbekannt"}
            </p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">
              Versicherung
            </label>
            <p className="text-sm text-gray-900">
              {patient.insuranceCompanyName || "Nicht angegeben"}
            </p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">
              Versicherungstyp
            </label>
            <p className="text-sm text-gray-900">
              {patient.insuranceType === "STATUTORY"
                ? "Gesetzlich"
                : patient.insuranceType === "PRIVATE"
                ? "Privat"
                : "Andere"}
            </p>
          </div>
          {patient.phone && (
            <div>
              <label className="text-sm font-medium text-gray-700">
                Telefon
              </label>
              <p className="text-sm text-gray-900">{patient.phone}</p>
            </div>
          )}
          {patient.email && (
            <div>
              <label className="text-sm font-medium text-gray-700">
                E-Mail
              </label>
              <p className="text-sm text-gray-900">{patient.email}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

// Search Component
const PatientSearch = ({ onSearch, searchTerm, setSearchTerm }) => {
  return (
    <div className="mb-6">
      <div className="flex gap-4">
        <div className="flex-1">
          <input
            type="text"
            placeholder="Patient suchen (Name, KVNR)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            onKeyPress={(e) => e.key === "Enter" && onSearch()}
          />
        </div>
        <button
          onClick={onSearch}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          Suchen
        </button>
      </div>
    </div>
  );
};

// System Health Component
const SystemHealth = () => {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchHealth = async () => {
      try {
        const healthData = await api.getSystemHealth();
        setHealth(healthData);
      } catch (error) {
        console.error("Health check failed:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchHealth();
    const interval = setInterval(fetchHealth, 30000); // Update every 30 seconds
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return (
      <div className="text-sm text-gray-500">Checking system health...</div>
    );
  }

  return (
    <div className="mb-6 p-4 bg-white rounded-lg shadow">
      <h3 className="text-sm font-medium text-gray-700 mb-2">System Status</h3>
      <div className="flex items-center gap-2">
        <div
          className={`w-3 h-3 rounded-full ${
            health?.status === "UP" ? "bg-green-500" : "bg-red-500"
          }`}
        ></div>
        <span className="text-sm text-gray-600">
          System: {health?.status || "UNKNOWN"}
        </span>
        {health?.components?.db && (
          <>
            <div
              className={`w-3 h-3 rounded-full ml-4 ${
                health.components.db.status === "UP"
                  ? "bg-green-500"
                  : "bg-red-500"
              }`}
            ></div>
            <span className="text-sm text-gray-600">
              DB: {health.components.db.status}
            </span>
          </>
        )}
      </div>
    </div>
  );
};

// Dashboard Stats Component
const DashboardStats = ({ patients }) => {
  const stats = {
    total: patients.length,
    male: patients.filter((p) => p.gender === "MALE").length,
    female: patients.filter((p) => p.gender === "FEMALE").length,
    statutory: patients.filter((p) => p.insuranceStatus === "ACTIVE").length,
  };

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
      <div className="bg-blue-50 p-4 rounded-lg">
        <div className="text-2xl font-bold text-blue-600">{stats.total}</div>
        <div className="text-sm text-blue-600">Patienten gesamt</div>
      </div>
      <div className="bg-green-50 p-4 rounded-lg">
        <div className="text-2xl font-bold text-green-600">{stats.male}</div>
        <div className="text-sm text-green-600">♂️ Männlich</div>
      </div>
      <div className="bg-pink-50 p-4 rounded-lg">
        <div className="text-2xl font-bold text-pink-600">{stats.female}</div>
        <div className="text-sm text-pink-600">♀️ Weiblich</div>
      </div>
      <div className="bg-purple-50 p-4 rounded-lg">
        <div className="text-2xl font-bold text-purple-600">
          {stats.statutory}
        </div>
        <div className="text-sm text-purple-600">Aktive Versicherung</div>
      </div>
    </div>
  );
};

// Main App Component
const HISFrontend = () => {
  const [patients, setPatients] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [selectedPatientDetails, setSelectedPatientDetails] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load all patients on mount
  useEffect(() => {
    loadPatients();
  }, []);

  const loadPatients = async () => {
    setLoading(true);
    setError(null);
    try {
      // Use search with empty term to get all patients
      const data = await api.searchPatients("", 0, 50);
      setPatients(data.content || []);
    } catch (error) {
      setError("Fehler beim Laden der Patienten: " + error.message);
      console.error("Error loading patients:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.searchPatients(searchTerm, 0, 50);
      setPatients(data.content || []);
    } catch (error) {
      setError("Fehler bei der Suche: " + error.message);
      console.error("Error searching patients:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectPatient = async (patient) => {
    setSelectedPatient(patient);
    try {
      // Load full patient details
      const details = await api.getPatient(patient.id);
      setSelectedPatientDetails(details);
    } catch (error) {
      setError("Fehler beim Laden der Patientendetails: " + error.message);
      console.error("Error loading patient details:", error);
    }
  };

  const handleBackToList = () => {
    setSelectedPatient(null);
    setSelectedPatientDetails(null);
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="py-6">
            <h1 className="text-3xl font-bold text-gray-900">
              🏥 Hospital Information System
            </h1>
            <p className="mt-2 text-gray-600">
              Frontend mit funktionierenden Backend-APIs
            </p>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <SystemHealth />

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
            <div className="text-red-800">{error}</div>
          </div>
        )}

        {!selectedPatient ? (
          <>
            <DashboardStats patients={patients} />

            <PatientSearch
              onSearch={handleSearch}
              searchTerm={searchTerm}
              setSearchTerm={setSearchTerm}
            />

            <PatientList
              patients={patients}
              onSelectPatient={handleSelectPatient}
              loading={loading}
            />
          </>
        ) : (
          <PatientDetail
            patient={selectedPatientDetails || selectedPatient}
            onBack={handleBackToList}
          />
        )}
      </div>
    </div>
  );
};

export default HISFrontend;
