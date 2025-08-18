\c his_db;

-- HIS Database Initialization Script
-- Erstellt Schemas für Patient und Encounter Services

-- Schemas erstellen (konsistent mit application.yml Files)
CREATE SCHEMA IF NOT EXISTS his_patient;
CREATE SCHEMA IF NOT EXISTS his_encounter;

-- Berechtigungen für his_user setzen (geteilter User)
GRANT ALL PRIVILEGES ON SCHEMA his_patient TO his_user;
GRANT ALL PRIVILEGES ON SCHEMA his_encounter TO his_user;

-- Existing Tables Permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA his_patient TO his_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA his_encounter TO his_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA his_patient TO his_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA his_encounter TO his_user;

-- Default Privileges für zukünftige Objekte
ALTER DEFAULT PRIVILEGES IN SCHEMA his_patient GRANT ALL ON TABLES TO his_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA his_encounter GRANT ALL ON TABLES TO his_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA his_patient GRANT ALL ON SEQUENCES TO his_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA his_encounter GRANT ALL ON SEQUENCES TO his_user;

-- Informative Ausgabe
\echo 'Database initialization completed:'
\echo '- Database: his_db'
\echo '- User: his_user'
\echo '- Schemas: his_patient, his_encounter'
\echo '- Permissions: ALL granted to his_user'

-- Schema-Übersicht anzeigen
SELECT schemaname
FROM information_schema.schemata
WHERE schemaname IN ('his_patient', 'his_encounter')
ORDER BY schemaname;

