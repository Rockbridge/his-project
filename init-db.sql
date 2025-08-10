\c pvs_db;
CREATE SCHEMA IF NOT EXISTS pvs_patient;
CREATE SCHEMA IF NOT EXISTS pvs_encounter;
GRANT ALL PRIVILEGES ON SCHEMA pvs_patient TO pvs_user;
GRANT ALL PRIVILEGES ON SCHEMA pvs_encounter TO pvs_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA pvs_patient GRANT ALL ON TABLES TO pvs_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA pvs_encounter GRANT ALL ON TABLES TO pvs_user;

-- HIS Database Initialization Script
-- Erstellt Schemas für Patient und Encounter Services

-- Mit der GETEILTEN Datenbank verbinden
\c his_db;

-- Schemas erstellen (konsistent mit application.yml Files)
CREATE SCHEMA IF NOT EXISTS his_patient;
CREATE SCHEMA IF NOT EXISTS his_encounter;

-- Berechtigungen für his_user setzen (geteilter User)
GRANT ALL PRIVILEGES ON SCHEMA his_patient TO his_user;
GRANT ALL PRIVILEGES ON SCHEMA his_encounter TO his_user;

-- Existing Tables Permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA patient TO encounter_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA encounter TO encounter_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA patient TO encounter_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA encounter TO encounter_user;

-- Default Privileges für zukünftige Objekte
ALTER DEFAULT PRIVILEGES IN SCHEMA patient GRANT ALL ON TABLES TO encounter_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA encounter GRANT ALL ON TABLES TO encounter_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA patient GRANT ALL ON SEQUENCES TO encounter_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA encounter GRANT ALL ON SEQUENCES TO encounter_user;

-- Informative Ausgabe
\echo 'Database initialization completed:'
\echo '- Database: his_encounter_db'
\echo '- User: encounter_user'
\echo '- Schemas: patient, encounter'
\echo '- Permissions: ALL granted to encounter_user'

-- Schema-Übersicht anzeigen
SELECT schemaname 
FROM information_schema.schemata 
WHERE schemaname IN ('patient', 'encounter')
ORDER BY schemaname;