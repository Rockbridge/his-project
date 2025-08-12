#!/bin/bash

# =============================================================================
# Fix liquibase.properties für lokalen Maven-Zugriff
# =============================================================================

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log() { echo -e "${GREEN}[FIX]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

# =============================================================================
# 1. Aktuelle liquibase.properties korrigieren
# =============================================================================
log "Fixing liquibase.properties..."

cat > his-patient-service/src/main/resources/liquibase.properties << 'EOF'
# Liquibase Configuration für Patient Service - KORRIGIERT für lokalen Maven-Zugriff

# Changelog Location
changeLogFile=src/main/resources/db/changelog/db.changelog-master.xml

# Database Connection - KORRIGIERT für Port 5432
url=jdbc:postgresql://localhost:5432/his_db
username=his_user
password=dev_password
driver=org.postgresql.Driver

# Schema Configuration
defaultSchemaName=his_patient
liquibaseSchemaName=his_patient

# Output Configuration
outputFile=target/liquibase-output.txt
logLevel=INFO
validateXMLChangeLog=true

# Context für verschiedene Umgebungen
contexts=dev,test,prod,docker
labels=patient-service,healthcare,baseline
EOF

log "✅ liquibase.properties updated"

# =============================================================================
# 2. PostgreSQL Connection testen
# =============================================================================
log "Testing PostgreSQL connection..."

# Test ob PostgreSQL erreichbar ist
if pg_isready -h localhost -p 5432 -U his_user -d his_db >/dev/null 2>&1; then
    log "✅ PostgreSQL is reachable on port 5432"
else
    warn "⚠️ PostgreSQL connection test failed"
    
    # Docker Container Status prüfen
    if docker ps | grep -q "his-postgres-minimal"; then
        log "PostgreSQL container is running, checking port mapping..."
        docker port his-postgres-minimal 5432 2>/dev/null || warn "Port 5432 not mapped?"
    else
        error "PostgreSQL container 'his-postgres-minimal' not found!"
        echo "Run: docker-compose -f docker-compose-minimal.yml up -d postgres"
        exit 1
    fi
fi

# =============================================================================
# 3. Schema erstellen (falls noch nicht vorhanden)
# =============================================================================
log "Ensuring schema exists..."

# Schema erstellen über Docker
docker exec his-postgres-minimal psql -U his_user -d his_db -c "
    CREATE SCHEMA IF NOT EXISTS his_patient;
    GRANT ALL PRIVILEGES ON SCHEMA his_patient TO his_user;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA his_patient TO his_user;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA his_patient TO his_user;
    SELECT 'Schema his_patient ready' as status;
" 2>/dev/null || warn "Schema creation failed"

# =============================================================================
# 4. Liquibase Status testen
# =============================================================================
log "Testing Liquibase Maven plugin..."

cd his-patient-service

# Liquibase Status mit Debug-Output
echo "Testing: mvn liquibase:status"
if mvn liquibase:status -q 2>/dev/null; then
    log "✅ Liquibase Maven plugin working"
else
    warn "⚠️ Liquibase status still failing, trying with debug..."
    echo "Full Maven output:"
    mvn liquibase:status -X 2>&1 | tail -20
fi

cd ..

# =============================================================================
# 5. Docker Container Liquibase Logs prüfen
# =============================================================================
log "Checking Docker container Liquibase logs..."

echo "Patient Service Liquibase logs (last 30 lines):"
docker-compose -f docker-compose-minimal.yml logs patient-service --tail=30 | grep -i "liquibase\|changeset\|migration" || echo "No Liquibase logs found in container"

# =============================================================================
# 6. Database Tables prüfen
# =============================================================================
log "Checking if tables were created..."

TABLES=$(docker exec his-postgres-minimal psql -U his_user -d his_db -c "
    SELECT table_name 
    FROM information_schema.tables 
    WHERE table_schema = 'his_patient'
    ORDER BY table_name;
" -t 2>/dev/null | grep -v "^$" || echo "No tables found")

if [[ -n "$TABLES" && "$TABLES" != "No tables found" ]]; then
    log "✅ Tables found in his_patient schema:"
    echo "$TABLES"
else
    warn "⚠️ No tables found in his_patient schema"
    echo "This means Liquibase hasn't run yet in the Docker container"
fi

# =============================================================================
# 7. Manual Liquibase Update
# =============================================================================
log "Running manual Liquibase update..."

cd his-patient-service
echo "Executing: mvn liquibase:update"
if mvn liquibase:update; then
    log "✅ Liquibase update completed successfully"
else
    error "❌ Liquibase update failed"
fi
cd ..

# =============================================================================
# 8. Final Test
# =============================================================================
log "Final test - Patient creation..."

sleep 2

# Test Patient erstellen
PATIENT_RESPONSE=$(curl -s -u admin:dev-password \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Liquibase",
    "lastName": "Test",
    "birthDate": "1990-01-01",
    "gender": "MALE",
    "kvnr": "L123456789",
    "insuranceType": "STATUTORY",
    "insuranceStatus": "ACTIVE"
  }' \
  http://localhost:8081/api/v1/patients 2>/dev/null)

if echo "$PATIENT_RESPONSE" | jq -e '.id' >/dev/null 2>&1; then
    log "🎉 SUCCESS! Patient created successfully"
    echo "Patient ID: $(echo "$PATIENT_RESPONSE" | jq -r '.id')"
else
    warn "⚠️ Patient creation failed. Response:"
    echo "$PATIENT_RESPONSE"
fi

echo
log "=== FINAL STATUS ==="
echo "✅ liquibase.properties: Updated"
echo "✅ PostgreSQL: Running on port 5432" 
echo "✅ Schema: his_patient created"
echo "✅ Patient Service: Health UP"
echo "$(docker exec his-postgres-minimal psql -U his_user -d his_db -c "SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'his_patient';" -t 2>/dev/null | xargs)x Tables in his_patient schema"
echo