#!/bin/bash

echo "🧪 Testing Enhanced Exception Handling"
echo "======================================"

BASE_URL="http://localhost:8080/api/v1/encounters"
AUTH_HEADER="Authorization: Basic $(echo -n 'admin:dev-password' | base64)"

# Test 1: Validation Error
echo "🔸 Test 1: Validation Error (Missing Fields)"
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{}' | jq '.code'

# Test 2: Business Rule Violation  
echo "🔸 Test 2: Business Rule Violation (Past Date)"
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "practitionerId": "550e8400-e29b-41d4-a716-446655440000", 
    "type": "CONSULTATION",
    "encounterDate": "2024-01-01T10:00:00",
    "billingContext": "GKV"
  }' | jq '.code'

# Test 3: Create valid encounter for state tests
echo "🔸 Test 3: Creating valid encounter"
ENCOUNTER_RESPONSE=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "practitionerId": "550e8400-e29b-41d4-a716-446655440000",
    "type": "CONSULTATION", 
    "encounterDate": "2025-08-15T14:00:00",
    "billingContext": "GKV"
  }')

ENCOUNTER_ID=$(echo $ENCOUNTER_RESPONSE | jq -r '.id')
echo "Created encounter: $ENCOUNTER_ID"

# Test 4: Start encounter (should work)
echo "🔸 Test 4: Start encounter (should work)"
curl -s -X PUT "$BASE_URL/$ENCOUNTER_ID/start" \
  -H "$AUTH_HEADER" | jq '.status'

# Test 5: Start already started encounter (should fail)
echo "🔸 Test 5: Start already started encounter (should fail)"
curl -s -X PUT "$BASE_URL/$ENCOUNTER_ID/start" \
  -H "$AUTH_HEADER" | jq '.code'

# Test 6: Complete encounter
echo "🔸 Test 6: Complete encounter"
curl -s -X PUT "$BASE_URL/$ENCOUNTER_ID/complete" \
  -H "$AUTH_HEADER" | jq '.status'

# Test 7: Complete already completed encounter (should fail)
echo "🔸 Test 7: Complete already completed encounter (should fail)" 
curl -s -X PUT "$BASE_URL/$ENCOUNTER_ID/complete" \
  -H "$AUTH_HEADER" | jq '.code'

# Test 8: Not found error
echo "🔸 Test 8: Not found error"
curl -s -X GET "$BASE_URL/00000000-0000-0000-0000-000000000000" \
  -H "$AUTH_HEADER" | jq '.code'

# Test 9: Method not allowed
echo "🔸 Test 9: Method not allowed"
curl -s -X DELETE $BASE_URL \
  -H "$AUTH_HEADER" | jq '.code'

echo ""
echo "✅ Exception handling tests completed!"
echo "Expected error codes:"
echo "- VALIDATION_ERROR"
echo "- PAST_ENCOUNTER_DATE (Business Rule)"
echo "- ENCOUNTER_ALREADY_STARTED"
echo "- ENCOUNTER_ALREADY_COMPLETED"
echo "- ENCOUNTER_NOT_FOUND"
echo "- METHOD_NOT_ALLOWED"