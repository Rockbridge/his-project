-- src/main/resources/db/migration/V4__Create_addresses_table.sql

CREATE TABLE his_patient.addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    person_id UUID NOT NULL,
    address_type VARCHAR(50) NOT NULL,
    street VARCHAR(200),
    house_number VARCHAR(20),
    postal_code VARCHAR(10),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(50),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    version BIGINT
);

ALTER TABLE his_patient.addresses
    ADD CONSTRAINT fk_addresses_person FOREIGN KEY (person_id)
    REFERENCES his_patient.persons(id) ON DELETE CASCADE;

-- Indizes
CREATE INDEX idx_addresses_person_id ON his_patient.addresses(person_id);
CREATE INDEX idx_addresses_postal_code ON his_patient.addresses(postal_code);