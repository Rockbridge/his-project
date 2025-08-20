#!/bin/zsh
# check-service-code.sh

echo "🔍 Checking service configuration..."

# 1. Prüfen ob Service überhaupt die richtige DB verwendet
echo "1. Database connection test:"
curl -s "http://localhost:8081/actuator/health/db" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" | jq '.'

# 2. Prüfen der JPA Konfiguration
echo "\n2. JPA Configuration:"
curl -s "http://localhost:8081/actuator/env/spring.jpa" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" | jq '.property.value'

# 3. Prüfen ob Transaction Manager aktiv ist
echo "\n3. Transaction Manager:"
curl -s "http://localhost:8081/actuator/beans" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" | jq '.contexts[].beans | to_entries[] | select(.key | contains("transaction"))' 2>/dev/null

# 4. Metrics prüfen
echo "\n4. HTTP Metrics (check for errors):"
curl -s "http://localhost:8081/actuator/metrics/http.server.requests" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" | jq '.measurements[] | select(.statistic == "COUNT" or .statistic == "TOTAL_TIME")'