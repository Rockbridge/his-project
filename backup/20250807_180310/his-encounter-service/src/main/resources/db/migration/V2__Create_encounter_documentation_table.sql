CREATE TABLE encounter_documentation (
    documentation_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    encounter_id UUID NOT NULL REFERENCES encounters(encounter_id) ON DELETE CASCADE,
    soap_section VARCHAR(50) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    content TEXT,
    structured_content JSONB,
    author_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indizes für SOAP-Dokumentation
CREATE INDEX idx_documentation_encounter ON encounter_documentation(encounter_id);
CREATE INDEX idx_documentation_soap_section ON encounter_documentation(encounter_id, soap_section);
CREATE INDEX idx_documentation_author ON encounter_documentation(author_id);
CREATE INDEX idx_documentation_date ON encounter_documentation(created_at);

-- JSONB Index für strukturierte Suchen
CREATE INDEX idx_documentation_structured_content ON encounter_documentation 
    USING GIN (structured_content);
