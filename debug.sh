#!/bin/bash

# ENCOUNTER VERIFICATION TEST
# ===========================
# Prüfe ob Frontend echte oder Dummy-Daten anzeigt

AUTH_HEADER="Authorization: Basic $(echo -n 'admin:dev-password' | base64)"

echo "🔍 ENCOUNTER VERIFICATION TEST"
echo "=============================="

# 1. Test Success Test Patient direkt
PATIENT_ID="2c74c930-516c-4135-b03e-7a9fa33cca72"

echo "1. Direct API Test für Success Test Patient:"
echo "Patient ID: $PATIENT_ID"
echo ""

# Direkte API Call wie Frontend
DIRECT_ENCOUNTERS=$(curl -s -H "$AUTH_HEADER" \
  "http://localhost:8080/api/v1/encounters/patient/$PATIENT_ID?page=0&size=10")

echo "Direct Encounter API Response:"
echo "$DIRECT_ENCOUNTERS" | jq '.'

ENCOUNTER_COUNT=$(echo "$DIRECT_ENCOUNTERS" | jq '.content | length' 2>/dev/null || echo "0")
echo ""
echo "✅ REAL Encounter Count: $ENCOUNTER_COUNT"

if [ "$ENCOUNTER_COUNT" -gt 0 ]; then
  echo ""
  echo "2. Encounter Details (REAL DATA):"
  echo "$DIRECT_ENCOUNTERS" | jq '.content[] | {
    id: .id[0:8] + "...",
    type: .type,
    status: .status,
    encounterDate: .encounterDate,
    reason: .reason,
    billingContext: .billingContext,
    practitionerId: .practitionerId[0:8] + "..."
  }'
  
  # 3. Prüfe ob Daten von unserem Test-Script stammen
  echo ""
  echo "3. Verification - Stammen diese von unserem Test-Script?"
  
  # Prüfe nach unserem Test-Grund
  TEST_REASON_COUNT=$(echo "$DIRECT_ENCOUNTERS" | jq '[.content[] | select(.reason == "Frontend Test - Korrigierte API")] | length' 2>/dev/null || echo "0")
  
  if [ "$TEST_REASON_COUNT" -gt 0 ]; then
    echo "✅ CONFIRMED: Encounters stammen von unserem Test-Script"
    echo "   Gefunden: $TEST_REASON_COUNT Encounters mit 'Frontend Test - Korrigierte API'"
  else
    echo "ℹ️ Encounters sind älter oder von anderem Test"
  fi
  
  # 4. Test letzter Encounter Datum
  echo ""
  echo "4. Last Encounter Test (Frontend Logic):"
  LAST_ENCOUNTER_DATE=$(echo "$DIRECT_ENCOUNTERS" | jq -r '.content[0]?.encounterDate // "null"')
  LAST_ENCOUNTER_STATUS=$(echo "$DIRECT_ENCOUNTERS" | jq -r '.content[0]?.status // "null"')
  
  echo "Frontend würde zeigen:"
  echo "  - Letzte Begegnung: $LAST_ENCOUNTER_DATE"
  echo "  - Patient Status: $([ "$LAST_ENCOUNTER_STATUS" = "IN_PROGRESS" ] && echo "In Behandlung" || echo "Aktiv")"
  
else
  echo "❌ PROBLEM: Keine Encounters gefunden!"
  echo "Das bedeutet Frontend zeigt möglicherweise Dummy-Daten oder Fallbacks"
fi

# 5. Test Browser Console Verification
echo ""
echo "🔍 BROWSER CONSOLE VERIFICATION:"
echo "================================"
echo "Öffne Browser Console und führe aus:"
echo ""
echo "// Test ob echte Daten geladen werden"
echo "fetch('http://localhost:8080/api/v1/encounters/patient/$PATIENT_ID?page=0&size=10', {"
echo "  headers: { 'Authorization': 'Basic $(echo -n 'admin:dev-password' | base64)' }"
echo "})"
echo ".then(r => r.json())"
echo ".then(d => {"
echo "  console.log('🔍 Direct API Result:', d);"
echo "  console.log('📊 Encounter Count:', d.content?.length || 0);"
echo "  if (d.content?.length > 0) {"
echo "    console.log('✅ REAL DATA found');"
echo "    console.log('📋 First Encounter:', d.content[0]);"
echo "  } else {"
echo "    console.log('❌ NO DATA - Frontend might show dummy data');"
echo "  }"
echo "});"

echo ""
echo "6. Alternative Verification:"
echo "============================"
echo "Im Browser Console, prüfe die loadPatientEncounters Funktion:"
echo ""
echo "// Teste direkt die Frontend Funktion"
echo "window.loadPatientEncounters?.('$PATIENT_ID')"
echo ".then(encounters => {"
echo "  console.log('📊 Frontend loaded encounters:', encounters.length);"
echo "  console.log('📋 Encounter details:', encounters);"
echo "  if (encounters.length > 0) {"
echo "    console.log('✅ Frontend loads REAL data');"
echo "  } else {"
echo "    console.log('❌ Frontend loads NO data - might be using fallbacks');"
echo "  }"
echo "});"

echo ""
echo "🎯 INTERPRETATION:"
echo "=================="
echo "Frontend zeigt ECHTE Daten wenn:"
echo "  ✅ API gibt encounters zurück ($ENCOUNTER_COUNT > 0)"
echo "  ✅ Browser Console zeigt 'encounters loaded' mit count > 0"
echo "  ✅ Encounter Details enthalten echte IDs, Daten, Status"
echo ""
echo "Frontend zeigt DUMMY Daten wenn:"
echo "  ❌ API gibt leere Antwort zurück"
echo "  ❌ Browser Console zeigt 'Failed to load encounters'"
echo "  ❌ Fallback-Logic wird verwendet"

echo ""
echo "📋 FINAL CHECK:"
echo "==============="
if [ "$ENCOUNTER_COUNT" -gt 0 ]; then
  echo "🎉 SUCCESS: Frontend sollte ECHTE Encounter Daten anzeigen!"
  echo "   - $ENCOUNTER_COUNT Encounters verfügbar"
  echo "   - API funktioniert korrekt"
  echo "   - Daten werden vom Backend geliefert"
else
  echo "⚠️ WARNING: Frontend könnte Dummy-Daten oder Fallbacks anzeigen"
  echo "   - Keine Encounters in API gefunden"
  echo "   - Prüfe ob loadPatientEncounters() Error Handling aktiviert wird"
fi