CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE encounters (
    encounter_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    practitioner_id UUID NOT NULL,
    encounter_type VARCHAR(50) NOT NULL,
    encounter_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    billing_context VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indizes für Performance
CREATE INDEX idx_encounters_patient_date ON encounters(patient_id, encounter_date DESC);
CREATE INDEX idx_encounters_practitioner ON encounters(practitioner_id);
CREATE INDEX idx_encounters_status ON encounters(status);
CREATE INDEX idx_encounters_date ON encounters(encounter_date);

-- Trigger für updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_encounters_updated_at
    BEFORE UPDATE ON encounters
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
