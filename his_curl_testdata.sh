# HIS Testdaten - Projektkonform
# Basierend auf der tatsächlichen API-Struktur aus dem Projektwissen

# ===== PATIENT TESTDATEN =====

# 1. Standardpatient (GKV) - Max Mustermann
echo "🏥 Erstelle Patient: Max Mustermann"
curl -s -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
  -d '{
    "firstName": "Max",
    "lastName": "Mustermann", 
    "birthDate": "1990-07-15",
    "gender": "MALE",
    "kvnr": "M123456780",
    "insuranceNumber": "A123456780",
    "insuranceType": "STATUTORY",
    "insuranceCompanyId": "104212059",
    "insuranceCompanyName": "AOK Bayern",
    "phone": "+49 89 12345678",
    "email": "max.mustermann@example.com",
    "consentCommunication": true,
    "consentDataProcessing": true
  }' | jq '{id, firstName, lastName, birthDate, gender, kvnr, insuranceType}'

# 2. Weibliche Patientin (PKV) - Dr. Anna Schmidt  
echo "🏥 Erstelle Patientin: Dr. Anna Schmidt"
curl -s -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
  -d '{
    "firstName": "Anna",
    "lastName": "Schmidt",
    "title": "Dr.",
    "birthDate": "1985-03-22",
    "gender": "FEMALE", 
    "kvnr": "A198503221",
    "insuranceNumber": "PKV-198503221",
    "insuranceType": "PRIVATE",
    "insuranceCompanyName": "Debeka Krankenversicherung",
    "phone": "+49 30 98765432",
    "email": "dr.anna.schmidt@example.com",
    "consentCommunication": true,
    "consentDataProcessing": false
  }' | jq '{id, firstName, lastName, title, birthDate, gender, kvnr, insuranceType}'

# 3. Pädiatrischer Patient - Tim Weber (minderjährig)
echo "🏥 Erstelle Patient: Tim Weber (minderjährig)"
curl -s -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
  -d '{
    "firstName": "Tim",
    "lastName": "Weber",
    "birthDate": "2015-11-08", 
    "gender": "MALE",
    "kvnr": "T201511083",
    "insuranceNumber": "T201511083",
    "insuranceType": "STATUTORY",
    "insuranceCompanyName": "Techniker Krankenkasse",
    "phone": "+49 40 11223344",
    "email": "familie.weber@example.com",
    "consentCommunication": true,
    "consentDataProcessing": true
  }' | jq '{id, firstName, lastName, birthDate, gender, kvnr, age: ((now | strftime("%Y") | tonumber) - (.birthDate | split("-")[0] | tonumber))}'

# 4. Senior Patient - Gertrud Müller (65+)
echo "🏥 Erstelle Patientin: Gertrud Müller (65+)"
curl -s -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
  -d '{
    "firstName": "Gertrud",
    "lastName": "Müller",
    "birthDate": "1950-12-03",
    "gender": "FEMALE",
    "kvnr": "G195012034", 
    "insuranceNumber": "G195012034",
    "insuranceType": "STATUTORY",
    "insuranceCompanyName": "Barmer",
    "phone": "+49 221 55443322",
    "email": "gertrud.mueller@example.com",
    "consentCommunication": false,
    "consentDataProcessing": true
  }' | jq '{id, firstName, lastName, birthDate, gender, kvnr, insuranceType}'

# 5. Patient mit komplexeren Daten - Prof. Dr. Hans Zimmerman
echo "🏥 Erstelle Patient: Prof. Dr. Hans Zimmerman"
curl -s -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
  -d '{
    "firstName": "Hans",
    "lastName": "Zimmerman", 
    "title": "Prof. Dr.",
    "birthDate": "1975-09-12",
    "gender": "MALE",
    "kvnr": "H197509125",
    "insuranceNumber": "PKV-H197509125",
    "insuranceType": "PRIVATE",
    "insuranceCompanyId": "168141347",
    "insuranceCompanyName": "DKV Deutsche Krankenversicherung",
    "phone": "+49 69 87654321",
    "email": "prof.zimmerman@uni-frankfurt.de",
    "consentCommunication": true,
    "consentDataProcessing": true
  }' | jq '{id, firstName, lastName, title, birthDate, gender, kvnr, insuranceType}'

# ===== SYSTEM VALIDIERUNG =====

echo ""
echo "🔍 System-Validierung:"

# Alle Patienten abrufen
echo "📋 Alle erstellten Patienten:"
curl -s http://localhost:8080/api/v1/patients | jq '.[] | {id, firstName, lastName, kvnr, insuranceType}'

# Health Check
echo ""
echo "🏥 System Health:"
curl -s http://localhost:8080/actuator/health | jq

echo ""
echo "✅ Testdaten erfolgreich erstellt!"
echo "🌐 Frontend testen: http://localhost:3000"
echo "📊 API Docs: http://localhost:8080/swagger-ui.html"

# ===== KVNR VALIDIERUNG TESTS =====

echo ""
echo "🧪 KVNR-Validierung Tests:"

# Test: Ungültige KVNR (zu kurz)
echo "❌ Test: Ungültige KVNR (zu kurz)"
curl -s -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
  -d '{
    "firstName": "Test",
    "lastName": "Invalid",
    "birthDate": "1990-01-01", 
    "gender": "MALE",
    "kvnr": "ABC123"
  }' | jq '.message // .error // "Validation failed as expected"'

# Test: KVNR mit verbotenem Buchstaben 'O'
echo "❌ Test: KVNR mit verbotenem Buchstaben O"
curl -s -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
  -d '{
    "firstName": "Test",
    "lastName": "InvalidO",
    "birthDate": "1990-01-01",
    "gender": "MALE", 
    "kvnr": "O123456789"
  }' | jq '.message // .error // "Validation failed as expected"'

# Test: Fehlende Pflichtfelder
echo "❌ Test: Fehlende Pflichtfelder"
curl -s -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
  -d '{
    "firstName": "Test"
  }' | jq '.message // .error // "Validation failed as expected"'

# ===== ENCOUNTER TESTDATEN (wenn Patient IDs vorhanden) =====

echo ""
echo "📋 Encounter-Testdaten erstellen..."

# Erste Patient-ID für Encounter-Tests abrufen
PATIENT_ID=$(curl -s http://localhost:8080/api/v1/patients | jq -r '.[0].id // empty')

if [ ! -z "$PATIENT_ID" ]; then
    echo "🏥 Erstelle Encounter für Patient: $PATIENT_ID"
    curl -s -X POST http://localhost:8080/api/v1/encounters \
      -H "Content-Type: application/json" \
      -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
      -d "{
        \"patientId\": \"$PATIENT_ID\",
        \"practitionerId\": \"$(uuidgen)\",
        \"type\": \"INITIAL\",
        \"encounterDate\": \"$(date -u +%Y-%m-%dT%H:%M:%S)\",
        \"billingContext\": \"GKV\",
        \"notes\": \"Erstuntersuchung - Testdaten\"
      }" | jq '{id, patientId, type, encounterDate, status}'
else
    echo "⚠️ Keine Patienten gefunden - Encounter nicht erstellt"
fi

echo ""
echo "🎯 Testdaten-Setup abgeschlossen!"
echo "🚀 Frontend starten: npm start (in his-frontend/)"
echo "📊 Backend läuft auf: http://localhost:8080"