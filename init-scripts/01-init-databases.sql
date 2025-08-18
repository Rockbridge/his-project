-- HIS Database Initialization Script
\c his_db;

-- Schemas erstellen
CREATE SCHEMA IF NOT EXISTS his_patient;
CREATE SCHEMA IF NOT EXISTS his_encounter;
CREATE SCHEMA IF NOT EXISTS his_auth;

-- Berechtigungen für his_user setzen
GRANT ALL PRIVILEGES ON SCHEMA his_patient TO his_user;
GRANT ALL PRIVILEGES ON SCHEMA his_encounter TO his_user;
GRANT ALL PRIVILEGES ON SCHEMA his_auth TO his_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA his_patient TO his_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA his_encounter TO his_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA his_auth TO his_user;

\echo 'Database initialization completed for his_db';