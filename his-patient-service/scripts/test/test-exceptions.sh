#!/bin/bash

# Patient Service Exception Testing - Curl Commands
# Base URL für Patient Service (anpassen falls nötig)
PATIENT_SERVICE_URL="http://localhost:8081"

echo "=== Testing Patient Service Exception Handling ==="
echo "Ensure Patient Service is running on $PATIENT_SERVICE_URL"
echo

# Make script executable
chmod +x scripts/test/test-exceptions.sh

# =========================================================================
# 1. PATIENT NOT FOUND SCENARIOS
# =========================================================================

echo "1. Testing Patient Not Found Scenarios"
echo "--------------------------------------"

# Test 1.1: Get non-existent patient by ID
echo "1.1 Non-existent Patient ID:"
curl -s -X GET "$PATIENT_SERVICE_URL/api/v1/patients/00000000-0000-0000-0000-000000000000" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" \
  -H "Content-Type: application/json" | jq

echo -e "\n"

# Test 1.2: Get non-existent patient by KVNR
echo "1.2 Non-existent KVNR:"
curl -s -X GET "$PATIENT_SERVICE_URL/api/v1/patients/kvnr/9999999999" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" \
  -H "Content-Type: application/json" | jq

echo -e "\n"

# =========================================================================
# 2. VALIDATION ERROR SCENARIOS  
# =========================================================================

echo "2. Testing Validation Error Scenarios"
echo "-------------------------------------"

# Test 2.1: Invalid JSON format
echo "2.1 Invalid JSON format:"
curl -s -X POST "$PATIENT_SERVICE_URL/api/v1/patients" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Max", "lastName": "Mustermann" invalid json}' | jq

echo -e "\n"

# Test 2.2: Missing required fields
echo "2.2 Missing required fields:"
curl -s -X POST "$PATIENT_SERVICE_URL/api/v1/patients" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Max"}' | jq

echo -e "\n"

# Test 2.3: Invalid KVNR format (too short)
echo "2.3 Invalid KVNR format (too short):"
curl -s -X POST "$PATIENT_SERVICE_URL/api/v1/patients" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Max",
    "lastName": "Mustermann", 
    "kvnr": "123",
    "dateOfBirth": "1980-05-15",
    "email": "max.mustermann@example.com"
  }' | jq

echo -e "\n"

echo "=== Exception Testing Complete ==="
echo "Review the responses above to verify proper error handling."
