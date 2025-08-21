#!/bin/bash

# HIS Encounter Service - Generate demo encounters via API Gateway
# Compatible with macOS and older Bash versions
# Author: Healthcare IT Team
# Date: $(date)

set -e

API_GATEWAY_URL="http://localhost:8080/api/v1"
AUTH_HEADER="Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk"
CONTENT_TYPE="Content-Type: application/json"

read -p "Wie viele Patienten sollen verwendet werden? " PATIENT_COUNT

# Fetch patient IDs through the API gateway
PATIENT_IDS=$(curl -s -H "$AUTH_HEADER" "$API_GATEWAY_URL/patients?page=0&size=$PATIENT_COUNT" | jq -r '( .content // . )[]? | .id')

if [ -z "$PATIENT_IDS" ]; then
    echo "Keine Patienten gefunden."
    exit 1
fi

types=(INITIAL CONSULTATION EMERGENCY ROUTINE_CHECKUP FOLLOW_UP SURGERY DIAGNOSTIC)
billings=(GKV PKV SELF_PAY BG)

for patient_id in $PATIENT_IDS; do
    encounter_count=$((RANDOM % 3 + 2))
    echo "\n👤 Patient $patient_id -> $encounter_count Encounters"
    for ((i=1; i<=encounter_count; i++)); do
        practitioner_id=$(uuidgen)
        type=${types[$RANDOM % ${#types[@]}]}
        billing=${billings[$RANDOM % ${#billings[@]}]}
        days_ago=$((RANDOM % 30))
        encounter_date=$(date -u -v-"${days_ago}"d +"%Y-%m-%dT%H:%M:%S")

        payload=$(cat <<JSON
{
  "patientId": "$patient_id",
  "practitionerId": "$practitioner_id",
  "type": "$type",
  "encounterDate": "$encounter_date",
  "billingContext": "$billing"
}
JSON
)
        response=$(curl -s -X POST "$API_GATEWAY_URL/encounters" -H "$AUTH_HEADER" -H "$CONTENT_TYPE" -d "$payload")
        if echo "$response" | jq -e '.id' > /dev/null; then
            echo "  ✅ Encounter $i erstellt"
        else
            echo "  ❌ Fehler bei Encounter $i: $(echo "$response" | jq -r '.message // .error // "Unknown error"')"
        fi
    done
done

echo "\nFertig!"
