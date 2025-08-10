#!/bin/bash

# ===========================================
# HIS SQL DEBUGGING - AUTH WORKS, NO DATA SAVED
# Authentication erfolgreich, aber keine INSERT statements
# ===========================================

echo "🔍 SQL DEBUGGING - AUTH OK, BUT NO DATA SAVED"
echo "=============================================="

NEW_PASSWORD="b0e91dc2-3090-4f2c-8fd2-98c8c94efc3c"
PATIENT_ID="ec3b64a8-f525-4e59-953d-839eb033398d"

# ===========================================
# 1. PROGRESS CONFIRMATION
# ===========================================

echo "1️⃣ PROGRESS CONFIRMATION"
echo "======================="

echo "✅ AUTHENTICATION: FIXED"
echo "• Keine HTTP 401 mehr"
echo "• Service akzeptiert requests"
echo "• Response kommt zurück (leere paginated result)"
echo ""
echo "❌ VERBLEIBENDES PROBLEM:"
echo "• Encounters werden nicht in PostgreSQL gespeichert"
echo "• Keine SQL INSERT statements in logs"
echo "• Silent failure in service logic"

# ===========================================
# 2. SQL LOGGING ANALYSIS
# ===========================================

echo ""
echo "2️⃣ SQL LOGGING ANALYSIS"
echo "======================"

echo "🔍 CHECK FOR SQL STATEMENTS:"
echo ""

echo "Check for any Hibernate SQL in logs:"
docker-compose -f docker-compose-minimal.yml logs encounter-service | grep -i "Hibernate:" | tail -10

echo ""
echo "Check for INSERT statements:"
docker-compose -f docker-compose-minimal.yml logs encounter-service | grep -i "INSERT" | tail -5

echo ""
echo "Check for any SQL activity:"
docker-compose -f docker-compose-minimal.yml logs encounter-service | grep -i "SQL" | tail -10

# ===========================================
# 3. REAL-TIME REQUEST MONITORING
# ===========================================

echo ""
echo "3️⃣ REAL-TIME REQUEST MONITORING"
echo "=============================="

echo "🧪 CREATE ENCOUNTER WITH LIVE LOG MONITORING:"
echo ""

# Create encounter and monitor logs immediately
echo "Creating encounter with live monitoring..."

curl -X POST "http://localhost:8082/api/v1/encounters" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n "user:${NEW_PASSWORD}" | base64)" \
  -d "{
    \"patientId\": \"${PATIENT_ID}\",
    \"practitionerId\": \"$(uuidgen)\",
    \"type\": \"EMERGENCY\",
    \"encounterDate\": \"$(date -u +%Y-%m-%dT%H:%M:%S)\",
    \"billingContext\": \"GKV\"
  }" && echo "Request sent!"

echo ""
echo "Checking logs immediately after request:"
docker-compose -f docker-compose-minimal.yml logs encounter-service --tail=20

# ===========================================
# 4. VALIDATION FAILURE TESTING
# ===========================================

echo ""
echo "4️⃣ VALIDATION FAILURE TESTING"
echo "============================="

echo "🧪 TEST @VALID ANNOTATION BEHAVIOR:"
echo ""

# Test with missing required fields (should trigger validation error)
echo "Testing with missing required field (should return 400):"
VALIDATION_RESPONSE=$(curl -s -w "HTTP_CODE:%{http_code}\n" \
  -X POST "http://localhost:8082/api/v1/encounters" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n "user:${NEW_PASSWORD}" | base64)" \
  -d "{\"patientId\":\"${PATIENT_ID}\"}" 2>/dev/null)

echo "Validation test response:"
echo "$VALIDATION_RESPONSE"

# If validation returns 400, then @Valid works
# If validation returns 200/201, then validation is not working

# ===========================================
# 5. SERVICE METHOD DEBUGGING
# ===========================================

echo ""
echo "5️⃣ SERVICE METHOD DEBUGGING"
echo "=========================="

echo "🔍 CHECK SERVICE LAYER LOGGING:"
echo ""

echo "Check for service method calls:"
docker-compose -f docker-compose-minimal.yml logs encounter-service | grep -i "Creating\|encounter\|service" | tail -10

echo ""
echo "Check for any DEBUG level logs:"
docker-compose -f docker-compose-minimal.yml logs encounter-service | grep "DEBUG" | tail -10

echo ""
echo "Check for any transaction logs:"
docker-compose -f docker-compose-minimal.yml logs encounter-service | grep -i "transaction\|commit\|rollback" | tail -5

# ===========================================
# 6. PATIENT VALIDATION TESTING
# ===========================================

echo ""
echo "6️⃣ PATIENT VALIDATION TESTING"
echo "============================="

echo "🔗 TEST FEIGN CLIENT FUNCTIONALITY:"
echo ""

echo "Test 1: Can encounter service reach patient service?"
docker exec his-encounter-service-minimal wget -q --spider http://patient-service:8081/actuator/health && echo "✅ Patient service reachable" || echo "❌ Patient service NOT reachable"

echo ""
echo "Test 2: Patient exists via direct API?"
PATIENT_EXISTS=$(curl -s "http://localhost:8081/api/v1/patients/${PATIENT_ID}" \
  -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" | jq -r '.id' 2>/dev/null)

if [ "$PATIENT_EXISTS" = "$PATIENT_ID" ]; then
    echo "✅ Patient exists: $PATIENT_ID"
else
    echo "❌ Patient NOT found or not accessible"
    echo "Response: $PATIENT_EXISTS"
fi

echo ""
echo "Test 3: Encounter with explicit patient validation:"
curl -X POST "http://localhost:8082/api/v1/encounters/with-patient-validation" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n "user:${NEW_PASSWORD}" | base64)" \
  -d "{
    \"patientId\": \"${PATIENT_ID}\",
    \"practitionerId\": \"$(uuidgen)\",
    \"type\": \"CONSULTATION\",
    \"encounterDate\": \"$(date -u +%Y-%m-%dT%H:%M:%S)\",
    \"billingContext\": \"GKV\"
  }"

echo ""
echo "Logs after patient validation test:"
docker-compose -f docker-compose-minimal.yml logs encounter-service --tail=10

# ===========================================
# 7. DETAILED ERROR ANALYSIS
# ===========================================

echo ""
echo "7️⃣ DETAILED ERROR ANALYSIS"
echo "=========================="

echo "🔍 COMPREHENSIVE LOG ANALYSIS:"
echo ""

echo "All recent encounter service activity:"
docker-compose -f docker-compose-minimal.yml logs encounter-service --since="5m" | tail -30

echo ""
echo "Any warnings or errors:"
docker-compose -f docker-compose-minimal.yml logs encounter-service | grep -i "warn\|error" | tail -10

echo ""
echo "Any exception traces:"
docker-compose -f docker-compose-minimal.yml logs encounter-service | grep -A 5 -B 5 "Exception" | tail -20

# ===========================================
# 8. MANUAL DEBUGGING COMMANDS
# ===========================================

echo ""
echo "8️⃣ MANUAL DEBUGGING COMMANDS"
echo "=========================="

echo "🚀 RUN THESE COMMANDS TO DEBUG:"
echo ""

echo "# 1. Monitor logs in real-time while making request:"
echo "# Terminal 1:"
echo "docker-compose -f docker-compose-minimal.yml logs encounter-service -f"
echo ""
echo "# Terminal 2:"
echo "curl -X POST 'http://localhost:8082/api/v1/encounters' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -H 'Authorization: Basic \$(echo -n \"user:${NEW_PASSWORD}\" | base64)' \\"
echo "  -d '{"
echo "    \"patientId\": \"${PATIENT_ID}\","
echo "    \"practitionerId\": \"'$(uuidgen)'\","
echo "    \"type\": \"CONSULTATION\","
echo "    \"encounterDate\": \"'$(date -u +%Y-%m-%dT%H:%M:%S)'\","
echo "    \"billingContext\": \"GKV\""
echo "  }'"
echo ""

echo "# 2. Check if validation is working:"
echo "curl -s -w 'HTTP_CODE:%{http_code}\\n' \\"
echo "  -X POST 'http://localhost:8082/api/v1/encounters' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -H 'Authorization: Basic \$(echo -n \"user:${NEW_PASSWORD}\" | base64)' \\"
echo "  -d '{\"patientId\":\"${PATIENT_ID}\"}'"
echo ""

echo "# 3. Test patient service connectivity:"
echo "docker exec his-encounter-service-minimal curl -s http://patient-service:8081/actuator/health"

# ===========================================
# 9. HYPOTHESIS SUMMARY
# ===========================================

echo ""
echo "9️⃣ HYPOTHESIS SUMMARY"
echo "===================="

echo "🎯 CURRENT HYPOTHESES:"
echo ""
echo "HYPOTHESIS A: VALIDATION SILENT FAILURE"
echo "• @Valid @RequestBody CreateEncounterRequest fails"
echo "• GlobalExceptionHandler returns empty response"
echo "• No service method execution"
echo ""
echo "HYPOTHESIS B: PATIENT VALIDATION FAILURE"
echo "• Feign client fails to reach patient service"
echo "• Service throws exception → transaction rollback"
echo "• Exception swallowed by try-catch"
echo ""
echo "HYPOTHESIS C: TRANSACTION ROLLBACK"
echo "• Service saves encounter successfully"
echo "• Later exception → automatic rollback"
echo "• Response sent before rollback"
echo ""
echo "HYPOTHESIS D: MAPPING PROBLEM"
echo "• Request body not mapping to CreateEncounterRequest"
echo "• Controller method not called"
echo "• No service execution"
echo ""
echo "✅ LOGS WILL REVEAL THE EXACT CAUSE!"

# ===========================================
# 10. NEXT STEPS
# ===========================================

echo ""
echo "🔟 NEXT STEPS"
echo "============="

echo "🎯 DEBUGGING PRIORITY:"
echo ""
echo "1. Monitor logs in real-time during request"
echo "2. Check if controller method is called"
echo "3. Check if service method is executed"
echo "4. Check if SQL statements are generated"
echo "5. Check for any exceptions or rollbacks"
echo ""
echo "🚀 START WITH: Real-time log monitoring while making request"