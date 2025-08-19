-- src/main/resources/db/migration/V3__Create_addresses_table.sql

CREATE TABLE his_patient.addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL,
    
    -- Adressdaten
    street VARCHAR(200) NOT NULL,
    house_number VARCHAR(20),
    postal_code VARCHAR(10) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(50) DEFAULT 'Deutschland',
    
    -- Adresstyp
    address_type VARCHAR(20) DEFAULT 'HOME', -- HOME, WORK, BILLING
    is_primary BOOLEAN DEFAULT false,
    
    -- Audit-Felder
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key
    CONSTRAINT fk_addresses_patient 
        FOREIGN KEY (patient_id) 
        REFERENCES his_patient.patients(id)
        ON DELETE CASCADE
);

-- Indizes
CREATE INDEX idx_addresses_patient_id ON his_patient.addresses(patient_id);
CREATE INDEX idx_addresses_postal_code ON his_patient.addresses(postal_code);

-- Constraint: Nur eine primäre Adresse pro Patient
CREATE UNIQUE INDEX idx_addresses_primary_per_patient 
    ON his_patient.addresses(patient_id)
    WHERE is_primary = true;