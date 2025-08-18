-- src/main/resources/db/migration/V2__Create_patients_table.sql

CREATE TABLE pvs_patient.patients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- Persönliche Daten
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    title VARCHAR(50),
    birth_date DATE NOT NULL,
    gender VARCHAR(10),
    
    -- Kontaktdaten
    phone VARCHAR(50),
    email VARCHAR(255),
    
    -- Versicherungsdaten (VSDM-konform)
    kvnr VARCHAR(10) NOT NULL UNIQUE,
    insurance_number VARCHAR(50),
    insurance_type VARCHAR(50),
    insurance_company_id VARCHAR(100),
    insurance_company_name VARCHAR(200),
    insurance_status VARCHAR(20) DEFAULT 'ACTIVE',
    
    -- Datenschutz
    consent_communication BOOLEAN DEFAULT false,
    consent_data_processing BOOLEAN DEFAULT false,
    
    -- Audit-Felder
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL -- Soft Delete
);

-- Indizes für Performance
CREATE INDEX idx_patients_kvnr ON pvs_patient.patients(kvnr);
CREATE INDEX idx_patients_last_name ON pvs_patient.patients(last_name);
CREATE INDEX idx_patients_birth_date ON pvs_patient.patients(birth_date);
CREATE INDEX idx_patients_deleted_at ON pvs_patient.patients(deleted_at);

-- Kommentare für Dokumentation
COMMENT ON TABLE pvs_patient.patients IS 'Patientenstammdaten gemäß VSDM-Standards';
COMMENT ON COLUMN pvs_patient.patients.kvnr IS 'Krankenversichertennummer (10-stellig)';
COMMENT ON COLUMN pvs_patient.patients.deleted_at IS 'Soft Delete Timestamp';