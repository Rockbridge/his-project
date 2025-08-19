-- src/main/resources/db/migration/V2__Create_persons_table.sql

CREATE TABLE his_patient.persons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Persönliche Daten
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    title VARCHAR(50),
    birth_date DATE,
    gender VARCHAR(20),
    phone VARCHAR(50),
    email VARCHAR(100),

    -- Audit-Felder
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    version BIGINT
);

-- Indizes
CREATE INDEX idx_persons_last_name ON his_patient.persons(last_name);
CREATE INDEX idx_persons_first_name ON his_patient.persons(first_name);
CREATE INDEX idx_persons_deleted_at ON his_patient.persons(deleted_at);