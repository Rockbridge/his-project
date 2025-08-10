-- Wird beim ersten Start der PostgreSQL-Container ausgeführt

-- UUID Extension aktivieren
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Development Seed Data (optional)
-- Beispiel-Practitioner für Development
-- Diese Tabellen werden später durch andere Services erstellt
-- Hier nur als Referenz für lokale Tests
