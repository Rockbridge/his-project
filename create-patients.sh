#!/bin/bash

# HIS Patient Service - 100 Test Patienten erstellen
# Für docker-compose-minimal Setup
# Kompatibel mit macOS und älteren Bash-Versionen
# Autor: Healthcare IT Team
# Datum: $(date)

set -e  # Exit on any error

# Configuration
API_GATEWAY_URL="http://localhost:8080"
PATIENT_SERVICE_URL="http://localhost:8081"
AUTH_HEADER="Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk"
CONTENT_TYPE="Content-Type: application/json"

# Use API Gateway by default, fallback to direct service
BASE_URL="$API_GATEWAY_URL"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# German names and data for realistic test patients
FIRST_NAMES_MALE=(
    "Max" "Alexander" "Paul" "Leon" "Luca" "Noah" "Felix" "Elias" "Jonas" "Luis"
    "Ben" "Finn" "Henry" "Tom" "Theo" "Emil" "Anton" "Moritz" "Samuel" "David"
    "Jakob" "Jonathan" "Matteo" "Niklas" "Jan" "Carl" "Leonard" "Adrian" "Julian" "Hannes"
    "Sebastian" "Michael" "Daniel" "Christian" "Stefan" "Thomas" "Markus" "Andreas" "Martin" "Florian"
    "Oliver" "Frank" "Peter" "Klaus" "Hans" "Günter" "Rolf" "Dieter" "Helmut" "Werner"
)

FIRST_NAMES_FEMALE=(
    "Emma" "Hannah" "Mia" "Sofia" "Lina" "Emilia" "Marie" "Lena" "Anna" "Lea"
    "Amelie" "Clara" "Maja" "Lia" "Ella" "Nora" "Ida" "Laura" "Greta" "Frieda"
    "Mathilda" "Luisa" "Paula" "Lara" "Zoe" "Charlotte" "Mila" "Elisabeth" "Josefine" "Victoria"
    "Julia" "Sandra" "Nicole" "Stefanie" "Andrea" "Sabine" "Petra" "Martina" "Birgit" "Claudia"
    "Susanne" "Monika" "Gabriele" "Ursula" "Christine" "Marion" "Karin" "Barbara" "Ingrid" "Helga"
)

LAST_NAMES=(
    "Müller" "Schmidt" "Schneider" "Fischer" "Weber" "Meyer" "Wagner" "Becker" "Schulz" "Hoffmann"
    "Schäfer" "Koch" "Bauer" "Richter" "Klein" "Wolf" "Schröder" "Neumann" "Schwarz" "Zimmermann"
    "Braun" "Krüger" "Hofmann" "Hartmann" "Lange" "Schmitt" "Werner" "Schmitz" "Krause" "Meier"
    "Lehmann" "Schmid" "Schulze" "Maier" "Köhler" "Herrmann" "König" "Walter" "Mayer" "Huber"
    "Kaiser" "Fuchs" "Peters" "Lang" "Scholz" "Möller" "Weiß" "Jung" "Hahn" "Schubert"
    "Winkler" "Berger" "Henkel" "Franke" "Albrecht" "Schuster" "Simon" "Ludwig" "Böhm" "Winter"
    "Kramer" "Martin" "Schumacher" "Krämer" "Voigt" "Stein" "Jäger" "Otto" "Sommer" "Groß"
    "Seidel" "Heinrich" "Brandt" "Haas" "Schreiber" "Graf" "Schulte" "Dietrich" "Ziegler" "Kuhn"
    "Kühn" "Pohl" "Engel" "Horn" "Busch" "Bergmann" "Vogt" "Sauer" "Arnold" "Wolff"
    "Pfeiffer" "Roth" "Lenz" "Bock" "Baumann" "Lorenz" "Lutz" "Günther" "Keller" "Beck"
)

TITLES=(
    "" "" "" "" "" "" "" ""  # 80% ohne Titel
    "Dr." "Dr."              # 20% Dr.
    "Prof. Dr." "Prof."      # Selten Professor
)

INSURANCE_COMPANIES=(
    "AOK Bayern" "AOK Baden-Württemberg" "AOK Nordost" "AOK Rheinland/Hamburg" "AOK PLUS"
    "Barmer" "DAK-Gesundheit" "Techniker Krankenkasse" "IKK classic" "KKH Kaufmännische Krankenkasse"
    "Axa Krankenversicherung" "Debeka" "DKV" "Allianz Private Krankenversicherung" "Barmenia"
    "HUK-COBURG" "Continentale" "Signal Iduna" "ERGO Direkt" "Gothaer"
)

CITIES=(
    "München" "Berlin" "Hamburg" "Frankfurt am Main" "Köln" "Stuttgart" "Düsseldorf" "Dortmund" 
    "Essen" "Leipzig" "Bremen" "Dresden" "Hannover" "Nürnberg" "Duisburg" "Bochum"
    "Wuppertal" "Bielefeld" "Bonn" "Münster" "Mannheim" "Augsburg" "Wiesbaden" "Karlsruhe"
    "Mönchengladbach" "Gelsenkirchen" "Aachen" "Braunschweig" "Chemnitz" "Kiel" "Halle" "Magdeburg"
    "Freiburg" "Krefeld" "Mainz" "Lübeck" "Oberhausen" "Erfurt" "Rostock" "Kassel"
    "Hagen" "Saarbrücken" "Hamm" "Mülheim" "Potsdam" "Ludwigshafen" "Oldenburg" "Leverkusen"
    "Osnabrück" "Solingen" "Heidelberg" "Herne" "Neuss" "Darmstadt" "Paderborn" "Regensburg"
    "Ingolstadt" "Würzburg" "Fürth" "Wolfsburg" "Offenbach" "Ulm" "Heilbronn" "Pforzheim"
    "Göttingen" "Bottrop" "Trier" "Recklinghausen" "Reutlingen" "Bremerhaven" "Koblenz" "Bergisch Gladbach"
    "Jena" "Remscheid" "Erlangen" "Moers" "Siegen" "Hildesheim" "Salzgitter" "Cottbus"
)

POSTAL_CODES=(
    "80331" "10115" "20095" "60311" "50667" "70173" "40213" "44135"
    "45127" "04109" "28195" "01067" "30159" "90402" "47051" "44787"
    "42103" "33602" "53111" "48143" "68159" "86150" "65183" "76133"
    "41061" "45879" "52062" "38100" "09111" "24103" "06108" "39104"
    "79098" "47798" "55116" "23552" "46042" "99084" "18055" "34117"
    "58095" "66111" "59065" "45468" "14467" "67059" "26122" "51373"
    "49074" "42651" "69115" "44623" "41460" "64283" "33098" "93047"
    "85049" "97070" "90762" "38440" "63065" "89081" "74072" "75175"
    "37073" "46236" "54290" "45657" "72764" "27568" "56068" "51428"
    "07743" "42853" "91052" "47441" "57072" "31134" "38226" "03046"
)

STREET_NAMES=(
    "Hauptstraße" "Kirchstraße" "Bahnhofstraße" "Gartenstraße" "Dorfstraße" "Schulstraße" "Poststraße"
    "Mühlenstraße" "Lindenstraße" "Bergstraße" "Friedhofstraße" "Feldstraße" "Waldstraße" "Ringstraße"
    "Am Markt" "Marktplatz" "Bachstraße" "Mittelstraße" "Neuer Weg" "Steinweg" "Mühlenweg"
    "Kirchweg" "Birkenweg" "Rosenstraße" "Tulpenstraße" "Nelkenstraße" "Lilienstraße" "Veilchenstraße"
    "Eichenstraße" "Buchenstraße" "Tannenstraße" "Fichtenstraße" "Ahornstraße" "Kastanienstraße"
    "Am Park" "Parkstraße" "Sportplatzstraße" "Schützenstraße" "Jahnstraße" "Goethestraße"
    "Schillerstraße" "Beethovenstraße" "Mozartstraße" "Bachstraße" "Brahmsstraße" "Händelstraße"
    "Bismarckstraße" "Hindenburgstraße" "Kaiser-Wilhelm-Straße" "Friedrich-Ebert-Straße"
    "Willy-Brandt-Straße" "Konrad-Adenauer-Straße" "John-F.-Kennedy-Platz" "Am Rathaus"
    "Rathausplatz" "Schlossstraße" "Burggasse" "Klostergasse" "Münzgasse" "Goldgasse"
    "Silbergasse" "Schmiedegasse" "Bäckergasse" "Fleischergasse" "Weberzunft" "Färbergasse"
    "Gerbergasse" "Töpfergasse" "Zimmermannsweg" "Maurerstraße" "Tischlerstraße" "Schreinerweg"
    "Am Fluss" "Uferstraße" "Seestraße" "Teichstraße" "Brückenstraße" "Dammstraße"
    "Höhenweg" "Talstraße" "Hangstraße" "Wiesenweg" "Ackerstraße" "Kornfeld" "Obstgarten"
    "Blumenwiese" "Sonnenstraße" "Mondstraße" "Sternstraße" "Regenbogenstraße" "Morgenröte"
    "Abendfrieden" "Zur Linde" "Zur Eiche" "Zur Buche" "Zur Tanne" "Zum Brunnen"
    "Am Brunnen" "Am Bach" "Am Teich" "Am See" "Am Wald" "Am Berg" "Am Tal" "Am Hang"
)

MOBILE_PREFIXES=("0151" "0152" "0157" "0159" "0160" "0162" "0163" "0170" "0171" "0172" "0173" "0174" "0175" "0176" "0177" "0178" "0179")
LANDLINE_AREA_CODES=("030" "040" "069" "089" "0221" "0211" "0201" "0231" "0234" "0511" "0341" "0351" "0361" "0371" "0381" "0421" "0431" "0451" "0511" "0531" "0561" "0621" "0631" "0681" "0711" "0721" "0761" "0821" "0841" "0911" "0921" "0951")

# Internet domains for email generation
EMAIL_DOMAINS=("gmail.com" "web.de" "gmx.de" "t-online.de" "yahoo.de" "outlook.de" "freenet.de" "arcor.de" "alice.de" "1und1.de")

# Function to generate random birth date
generate_birth_date() {
    local min_year=1930
    local max_year=2010
    local year=$((min_year + RANDOM % (max_year - min_year + 1)))
    local month=$((1 + RANDOM % 12))
    local day=$((1 + RANDOM % 28))  # Use 28 to avoid month-specific day issues
    
    printf "%04d-%02d-%02d" $year $month $day
}

# Function to generate KVNR
generate_kvnr() {
    local letters="ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    local letter=${letters:$((RANDOM % 26)):1}
    local numbers=$(printf "%09d" $((RANDOM % 1000000000)))
    echo "${letter}${numbers}"
}

# Function to generate mobile phone number
generate_mobile_phone() {
    local prefix=${MOBILE_PREFIXES[$((RANDOM % ${#MOBILE_PREFIXES[@]}))]}
    local number=$(printf "%08d" $((RANDOM % 100000000)))
    echo "+49 ${prefix} ${number:0:4} ${number:4:4}"
}

# Function to generate landline phone number
generate_landline_phone() {
    local area_code=${LANDLINE_AREA_CODES[$((RANDOM % ${#LANDLINE_AREA_CODES[@]}))]}
    local number_length=$((6 + RANDOM % 3))  # 6-8 digits
    if command -v bc &> /dev/null; then
        local max_number=$(echo "10^${number_length}" | bc)
        local number=$(printf "%0${number_length}d" $((RANDOM % max_number)))
    else
        local number=$(printf "%0${number_length}d" $((RANDOM % 10000000)))
    fi
    echo "+49 ${area_code} ${number}"
}

# Function to get state for city (German Bundesländer)
get_state_for_city() {
    local city="$1"
    case "$city" in
        "München"|"Augsburg"|"Nürnberg"|"Würzburg"|"Regensburg"|"Ingolstadt"|"Fürth")
            echo "Bayern" ;;
        "Berlin")
            echo "Berlin" ;;
        "Hamburg"|"Bremen"|"Bremerhaven")
            echo "Hamburg" ;;
        "Köln"|"Düsseldorf"|"Dortmund"|"Essen"|"Duisburg"|"Bochum"|"Wuppertal"|"Bielefeld"|"Bonn"|"Münster"|"Mönchengladbach"|"Gelsenkirchen"|"Aachen"|"Krefeld"|"Oberhausen"|"Hagen"|"Hamm"|"Mülheim"|"Neuss"|"Herne"|"Solingen"|"Leverkusen"|"Paderborn"|"Bottrop"|"Recklinghausen"|"Remscheid"|"Moers"|"Siegen"|"Bergisch Gladbach")
            echo "Nordrhein-Westfalen" ;;
        "Stuttgart"|"Mannheim"|"Karlsruhe"|"Freiburg"|"Heidelberg"|"Ulm"|"Heilbronn"|"Pforzheim"|"Reutlingen")
            echo "Baden-Württemberg" ;;
        "Frankfurt am Main"|"Wiesbaden"|"Kassel"|"Darmstadt"|"Offenbach")
            echo "Hessen" ;;
        "Leipzig"|"Dresden"|"Chemnitz")
            echo "Sachsen" ;;
        "Hannover"|"Braunschweig"|"Oldenburg"|"Osnabrück"|"Göttingen"|"Wolfsburg"|"Salzgitter"|"Hildesheim")
            echo "Niedersachsen" ;;
        "Kiel"|"Lübeck")
            echo "Schleswig-Holstein" ;;
        "Magdeburg"|"Halle")
            echo "Sachsen-Anhalt" ;;
        "Erfurt"|"Jena")
            echo "Thüringen" ;;
        "Potsdam"|"Cottbus")
            echo "Brandenburg" ;;
        "Schwerin"|"Rostock")
            echo "Mecklenburg-Vorpommern" ;;
        "Saarbrücken")
            echo "Saarland" ;;
        "Mainz"|"Ludwigshafen"|"Koblenz"|"Trier")
            echo "Rheinland-Pfalz" ;;
        *)
            echo "Bayern" ;;  # Default fallback
    esac
}

# Function to generate address
generate_address() {
    local street=${STREET_NAMES[$((RANDOM % ${#STREET_NAMES[@]}))]}
    local house_number=$((1 + RANDOM % 200))
    local house_suffix=""
    
    # 20% chance for house number suffix (a, b, c)
    if [ $((RANDOM % 5)) -eq 0 ]; then
        local suffixes=("a" "b" "c")
        house_suffix=${suffixes[$((RANDOM % 3))]}
    fi
    
    local postal_code=${POSTAL_CODES[$((RANDOM % ${#POSTAL_CODES[@]}))]}
    local city=${CITIES[$((RANDOM % ${#CITIES[@]}))]}
    
    echo "${street}|${house_number}${house_suffix}|${postal_code}|${city}"
}

# Function to generate email address
generate_email() {
    local first_name="$1"
    local last_name="$2"
    local birth_year="$3"
    
    # Convert to lowercase using tr
    local first_lower=$(echo "$first_name" | tr '[:upper:]' '[:lower:]')
    local last_lower=$(echo "$last_name" | tr '[:upper:]' '[:lower:]')
    
    # Replace umlauts
    first_lower=$(echo "$first_lower" | sed 's/ä/ae/g; s/ö/oe/g; s/ü/ue/g; s/ß/ss/g')
    last_lower=$(echo "$last_lower" | sed 's/ä/ae/g; s/ö/oe/g; s/ü/ue/g; s/ß/ss/g')
    
    local domain=${EMAIL_DOMAINS[$((RANDOM % ${#EMAIL_DOMAINS[@]}))]}
    
    # Different email patterns
    local patterns=(
        "${first_lower}.${last_lower}"
        "${first_lower}_${last_lower}"
        "${first_lower}${last_lower}"
        "${first_lower}.${last_lower}${birth_year}"
        "${first_lower}${last_lower}${birth_year}"
        "${last_lower}.${first_lower}"
    )
    
    local pattern=${patterns[$((RANDOM % ${#patterns[@]}))]}
    echo "${pattern}@${domain}"
}

# Function to check if service is available
check_service() {
    echo -e "${BLUE}🔍 Checking service availability...${NC}"
    
    # Try API Gateway first
    if curl -s -f -H "$AUTH_HEADER" "$API_GATEWAY_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ API Gateway is available (Port 8080)${NC}"
        BASE_URL="$API_GATEWAY_URL"
        return 0
    fi
    
    # Fallback to direct Patient Service
    if curl -s -f -H "$AUTH_HEADER" "$PATIENT_SERVICE_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Using direct Patient Service (Port 8081) - API Gateway not available${NC}"
        BASE_URL="$PATIENT_SERVICE_URL"
        return 0
    fi
    
    echo -e "${RED}❌ Neither API Gateway nor Patient Service is available!${NC}"
    echo -e "${YELLOW}💡 Please ensure docker-compose-minimal.yml is running:${NC}"
    echo -e "${YELLOW}   docker-compose -f docker-compose-minimal.yml up -d${NC}"
    exit 1
}

# Function to create JSON with proper escaping
create_json_payload() {
    local first_name="$1"
    local last_name="$2"
    local title="$3"
    local birth_date="$4"
    local gender="$5"
    local kvnr="$6"
    local insurance_number="$7"
    local insurance_type="$8"
    local insurance_company_id="$9"
    local insurance_company="${10}"
    local street="${11}"
    local house_number="${12}"
    local postal_code="${13}"
    local city="${14}"
    local state="${15}"
    local phone="${16}"
    local mobile="${17}"
    local email="${18}"
    local consent="${19}"
    
    # Escape JSON strings properly
    first_name=$(echo "$first_name" | sed 's/"/\\"/g')
    last_name=$(echo "$last_name" | sed 's/"/\\"/g')
    title=$(echo "$title" | sed 's/"/\\"/g')
    insurance_company=$(echo "$insurance_company" | sed 's/"/\\"/g')
    street=$(echo "$street" | sed 's/"/\\"/g')
    city=$(echo "$city" | sed 's/"/\\"/g')
    state=$(echo "$state" | sed 's/"/\\"/g')
    
    # Build JSON step by step
    local json='{'
    json="${json}\"firstName\":\"${first_name}\""
    json="${json},\"lastName\":\"${last_name}\""
    
    if [ -n "$title" ]; then
        json="${json},\"title\":\"${title}\""
    fi
    
    json="${json},\"birthDate\":\"${birth_date}\""
    json="${json},\"gender\":\"${gender}\""
    json="${json},\"kvnr\":\"${kvnr}\""
    json="${json},\"insuranceNumber\":\"${insurance_number}\""
    json="${json},\"insuranceStatus\":\"ACTIVE\""
    json="${json},\"insuranceType\":\"${insurance_type}\""
    json="${json},\"insuranceCompanyId\":\"${insurance_company_id}\""
    json="${json},\"insuranceCompanyName\":\"${insurance_company}\""
    
    # Add addresses array
    json="${json},\"addresses\":[{"
    json="${json}\"addressType\":\"PRIMARY\""
    json="${json},\"street\":\"${street}\""
    json="${json},\"houseNumber\":\"${house_number}\""
    json="${json},\"postalCode\":\"${postal_code}\""
    json="${json},\"city\":\"${city}\""
    json="${json},\"state\":\"${state}\""
    json="${json},\"country\":\"Deutschland\""
    json="${json}}]"
    
    # Add optional contact fields
    if [ -n "$phone" ]; then
        json="${json},\"phone\":\"${phone}\""
    fi
    
    if [ -n "$mobile" ]; then
        json="${json},\"mobile\":\"${mobile}\""
    fi
    
    if [ -n "$email" ]; then
        json="${json},\"email\":\"${email}\""
    fi
    
    json="${json},\"consentCommunication\":${consent}"
    json="${json},\"consentDataProcessing\":true"
    json="${json}}"
    
    echo "$json"
}

# Function to create a single patient
create_patient() {
    local index=$1
    
    # Randomly select gender
    local genders=("MALE" "FEMALE")
    local gender=${genders[$((RANDOM % 2))]}
    
    # Select names based on gender
    if [ "$gender" = "MALE" ]; then
        local first_name=${FIRST_NAMES_MALE[$((RANDOM % ${#FIRST_NAMES_MALE[@]}))]}
    else
        local first_name=${FIRST_NAMES_FEMALE[$((RANDOM % ${#FIRST_NAMES_FEMALE[@]}))]}
    fi
    
    local last_name=${LAST_NAMES[$((RANDOM % ${#LAST_NAMES[@]}))]}
    local title=${TITLES[$((RANDOM % ${#TITLES[@]}))]}
    local birth_date=$(generate_birth_date)
    local kvnr=$(generate_kvnr)
    
    # Generate insurance data
    local insurance_types=("STATUTORY" "PRIVATE" "OTHER")
    local insurance_type=${insurance_types[$((RANDOM % 3))]}
    local insurance_company=${INSURANCE_COMPANIES[$((RANDOM % ${#INSURANCE_COMPANIES[@]}))]}
    local insurance_number=$(printf "%09d" $((100000000 + RANDOM % 900000000)))
    local insurance_company_id=$(printf "%08d" $((10000000 + RANDOM % 90000000)))
    
    # Generate address data
    local address_data=$(generate_address)
    local street=$(echo "$address_data" | cut -d'|' -f1)
    local house_number=$(echo "$address_data" | cut -d'|' -f2)
    local postal_code=$(echo "$address_data" | cut -d'|' -f3)
    local city=$(echo "$address_data" | cut -d'|' -f4)
    local state=$(get_state_for_city "$city")
    
    # Extract birth year for email generation
    local birth_year=$(echo "$birth_date" | cut -d'-' -f1)
    
    # Generate contact data (95% get phone, 85% get mobile, 75% get email)
    local phone=""
    local mobile=""
    local email=""
    
    # 95% chance for landline phone
    if [ $((RANDOM % 100)) -lt 95 ]; then
        phone=$(generate_landline_phone)
    fi
    
    # 85% chance for mobile phone
    if [ $((RANDOM % 100)) -lt 85 ]; then
        mobile=$(generate_mobile_phone)
    fi
    
    # 75% chance for email
    if [ $((RANDOM % 100)) -lt 75 ]; then
        email=$(generate_email "$first_name" "$last_name" "$birth_year")
    fi
    
    # Generate consent
    local consent=$([ $((RANDOM % 10)) -lt 8 ] && echo "true" || echo "false")
    
    # Create JSON payload
    local json_payload=$(create_json_payload \
        "$first_name" "$last_name" "$title" "$birth_date" "$gender" "$kvnr" \
        "$insurance_number" "$insurance_type" "$insurance_company_id" "$insurance_company" \
        "$street" "$house_number" "$postal_code" "$city" "$state" \
        "$phone" "$mobile" "$email" "$consent")
    
    # Make API call
    local response=$(curl -s -X POST "${BASE_URL}/api/v1/patients" \
        -H "$AUTH_HEADER" \
        -H "$CONTENT_TYPE" \
        -d "$json_payload")
    
    # Check if creation was successful
    if echo "$response" | jq -e '.id' > /dev/null 2>&1; then
        local patient_id=$(echo "$response" | jq -r '.id')
        local contact_info=""
        [ -n "$mobile" ] && contact_info="📱"
        [ -n "$email" ] && contact_info="${contact_info}📧"
        echo -e "${GREEN}✅ Patient $index: $first_name $last_name ($city) $contact_info (ID: ${patient_id:0:8}...)${NC}"
        return 0
    else
        echo -e "${RED}❌ Patient $index failed: $first_name $last_name${NC}"
        echo -e "${RED}   Error: $(echo "$response" | jq -r '.message // .error // "Unknown error"')${NC}"
        return 1
    fi
}

# Main execution
main() {
    echo -e "${BLUE}🏥 HIS Patient Service - Creating 100 Test Patients${NC}"
    echo -e "${BLUE}=================================================${NC}"
    echo ""
    
    # Check service availability
    check_service
    echo ""
    
    # Initialize counters
    local successful=0
    local failed=0
    local start_time=$(date +%s)
    
    echo -e "${BLUE}🚀 Starting patient creation...${NC}"
    echo ""
    
    # Create patients
    for i in $(seq 1 100); do
        if create_patient $i; then
            ((successful++))
        else
            ((failed++))
        fi
        
        # Progress indicator every 10 patients
        if [ $((i % 10)) -eq 0 ]; then
            echo -e "${YELLOW}📊 Progress: $i/100 patients processed (✅ $successful success, ❌ $failed failed)${NC}"
            echo ""
        fi
        
        # Small delay to avoid overwhelming the service
        sleep 0.1
    done
    
    # Final statistics
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    echo ""
    echo -e "${BLUE}📋 FINAL STATISTICS${NC}"
    echo -e "${BLUE}==================${NC}"
    echo -e "${GREEN}✅ Successfully created: $successful patients${NC}"
    echo -e "${RED}❌ Failed: $failed patients${NC}"
    echo -e "${YELLOW}⏱️  Total time: ${duration}s${NC}"
    echo -e "${YELLOW}📊 Rate: $(echo "scale=2; $successful/$duration" | bc 2>/dev/null || echo "N/A") patients/second${NC}"
    echo ""
    
    # Show some sample patients
    if [ $successful -gt 0 ]; then
        echo -e "${BLUE}👥 Sample patients created:${NC}"
        curl -s -H "$AUTH_HEADER" "${BASE_URL}/api/v1/patients?page=0&size=5" | \
            jq -r '.content[]? | "   • \(.firstName) \(.lastName) - \(.kvnr)"'
        echo ""
        
        echo -e "${YELLOW}💡 You can view all patients with full data:${NC}"
        echo -e "${YELLOW}   curl -s -H '$AUTH_HEADER' '${BASE_URL}/api/v1/patients?page=0&size=20' | jq '.content[]'${NC}"
        echo ""
        
        echo -e "${YELLOW}📝 Note: Addresses may not be visible due to current Patient Service implementation${NC}"
        echo -e "${YELLOW}   The address data is being sent correctly in the expected format.${NC}"
    fi
}

# Check dependencies
if ! command -v curl &> /dev/null; then
    echo -e "${RED}❌ curl is required but not installed.${NC}"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ jq is required but not installed.${NC}"
    echo -e "${YELLOW}   On macOS: brew install jq${NC}"
    echo -e "${YELLOW}   On Ubuntu: sudo apt install jq${NC}"
    exit 1
fi

if ! command -v bc &> /dev/null; then
    echo -e "${YELLOW}⚠️  bc not found - statistics calculation may be limited${NC}"
fi

# Run main function
main "$@"