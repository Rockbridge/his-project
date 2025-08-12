#!/bin/bash

# =============================================================================
# Fix für docker-compose-minimal.yml - Liquibase aktivieren
# =============================================================================

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log() { echo -e "${GREEN}[FIX]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }

# =============================================================================
# 1. Backup der originalen docker-compose-minimal.yml
# =============================================================================
log "Creating backup of docker-compose-minimal.yml..."
cp docker-compose-minimal.yml docker-compose-minimal.yml.backup-$(date +%Y%m%d-%H%M%S)

# =============================================================================
# 2. Patient Service Environment Variables patchen
# =============================================================================
log "Patching Patient Service environment variables..."

# DDL-Auto von update auf none ändern
sed -i.tmp 's/SPRING_JPA_HIBERNATE_DDL_AUTO: update/SPRING_JPA_HIBERNATE_DDL_AUTO: none/' docker-compose-minimal.yml

# Liquibase Environment Variablen hinzufügen (nach der JPA-Konfiguration)
perl -i -pe '
if (/SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA: his_patient/) {
    $_ .= "      # LIQUIBASE AKTIVIEREN - HINZUGEFÜGT\n";
    $_ .= "      SPRING_LIQUIBASE_ENABLED: true\n";
    $_ .= "      SPRING_LIQUIBASE_CHANGE_LOG: classpath:db/changelog/db.changelog-master.xml\n";
    $_ .= "      SPRING_LIQUIBASE_DEFAULT_SCHEMA: his_patient\n";
    $_ .= "      SPRING_LIQUIBASE_CONTEXTS: docker,production\n";
}
' docker-compose-minimal.yml

# Liquibase Logging hinzufügen (nach dem Root-Logging)
perl -i -pe '
if (/LOGGING_LEVEL_ROOT: INFO/) {
    $_ .= "      LOGGING_LEVEL_LIQUIBASE: INFO\n";
}
' docker-compose-minimal.yml

# Temporäre Datei löschen
rm -f docker-compose-minimal.yml.tmp

log "✅ docker-compose-minimal.yml updated"

# =============================================================================
# 3. PostgreSQL Schema sicherstellen
# =============================================================================
log "Ensuring PostgreSQL schema exists..."

# PostgreSQL Container prüfen
if docker ps | grep -q "his-postgres-minimal"; then
    log "PostgreSQL container is running, creating schema..."
    docker exec his-postgres-minimal psql -U his_user -d his_db -c "
        CREATE SCHEMA IF NOT EXISTS his_patient;
        GRANT ALL PRIVILEGES ON SCHEMA his_patient TO his_user;
        GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA his_patient TO his_user;
        GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA his_patient TO his_user;
    " 2>/dev/null || true
    log "✅ Schema his_patient ensured"
else
    warn "PostgreSQL container not running. Schema will be created on first start."
fi

# =============================================================================
# 4. Services neustarten
# =============================================================================
log "Restarting Patient Service..."

# Patient Service neustarten
docker-compose -f docker-compose-minimal.yml restart patient-service

# Kurz warten
sleep 10

# =============================================================================
# 5. Validierung
# =============================================================================
log "Validating Liquibase activation..."

# Logs checken für Liquibase
if docker-compose -f docker-compose-minimal.yml logs patient-service --tail=50 | grep -q "liquibase"; then
    log "✅ Liquibase logs detected in Patient Service"
else
    warn "⚠️ No Liquibase logs detected yet - check manually"
fi

# Health Check
sleep 5
HEALTH=$(curl -s http://localhost:8081/actuator/health 2>/dev/null | jq -r '.status // "ERROR"' 2>/dev/null || echo "ERROR")
if [[ "$HEALTH" == "UP" ]]; then
    log "✅ Patient Service health check: $HEALTH"
else
    warn "⚠️ Patient Service health check: $HEALTH"
fi

# =============================================================================
# 6. Test Liquibase Status
# =============================================================================
log "Testing Liquibase status..."

cd his-patient-service
if mvn liquibase:status -q 2>/dev/null; then
    log "✅ Liquibase status check successful"
else
    warn "⚠️ Liquibase status check failed - check configuration"
fi
cd ..

# =============================================================================
# Abschluss
# =============================================================================
echo
log "🎉 Docker Compose Minimal Fix completed!"
echo
echo "Next steps:"
echo "1. Check Patient Service logs:"
echo "   docker-compose -f docker-compose-minimal.yml logs patient-service | grep liquibase"
echo
echo "2. Test API:"
echo "   curl -u admin:dev-password http://localhost:8081/actuator/health | jq"
echo
echo "3. Create test patient:"
echo "   curl -u admin:dev-password -X POST -H 'Content-Type: application/json' \\"
echo "   -d '{\"firstName\":\"Test\",\"lastName\":\"Patient\",\"birthDate\":\"1990-01-01\",\"gender\":\"MALE\",\"kvnr\":\"T123456789\"}' \\"
echo "   http://localhost:8081/api/v1/patients | jq"
echo