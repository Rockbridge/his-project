-- src/main/resources/db/migration/V3__Create_patients_table.sql

CREATE TABLE his_patient.patients (
    id UUID PRIMARY KEY,
    kvnr VARCHAR(10) NOT NULL UNIQUE,
    insurance_number VARCHAR(50),
    insurance_type VARCHAR(50),
    insurance_company_id VARCHAR(100),
    insurance_company_name VARCHAR(200),
    insurance_status VARCHAR(20) DEFAULT 'ACTIVE',
    consent_communication BOOLEAN DEFAULT false,
    consent_data_processing BOOLEAN DEFAULT false
);

ALTER TABLE his_patient.patients
    ADD CONSTRAINT fk_patients_person FOREIGN KEY (id)
    REFERENCES his_patient.persons(id) ON DELETE CASCADE;

-- Indizes
CREATE INDEX idx_patients_kvnr ON his_patient.patients(kvnr);
