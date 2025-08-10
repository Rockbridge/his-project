#!/bin/bash
echo "👥 Creating realistic test patients with valid KVNRs..."

# Deutsche Krankenkassen Array
declare -a insurance_companies=(
    "AOK Bayern"
    "Techniker Krankenkasse" 
    "Barmer"
    "DAK-Gesundheit"
    "IKK classic"
    "Debeka"
    "HUK-COBURG"
    "Signal Iduna"
)

# Realistische Namen Arrays
declare -a male_names=("Max" "Alexander" "Thomas" "Michael" "Christian" "Andreas" "Stefan" "Markus")
declare -a female_names=("Anna" "Maria" "Sandra" "Julia" "Petra" "Sabine" "Andrea" "Nicole")
declare -a surnames=("Müller" "Schmidt" "Schneider" "Fischer" "Weber" "Meyer" "Wagner" "Becker")

create_realistic_patient() {
    local index=$1
    local gender=$2
    
    # Name selection based on gender
    if [ "$gender" = "MALE" ]; then
        local first_name=${male_names[$((index % ${#male_names[@]}))]}
        local kvnr_prefix="M"
    else
        local first_name=${female_names[$((index % ${#female_names[@]}))]}
        local kvnr_prefix="A"
    fi
    
    local last_name=${surnames[$((index % ${#surnames[@]}))]}
    local insurance=${insurance_companies[$((index % ${#insurance_companies[@]}))]}
    
    # Generate realistic birth date (20-80 years old)
    local birth_year=$((1945 + RANDOM % 60))
    local birth_month=$(printf "%02d" $((1 + RANDOM % 12)))
    local birth_day=$(printf "%02d" $((1 + RANDOM % 28)))
    local birth_date="${birth_year}-${birth_month}-${birth_day}"
    
    # Generate KVNR with birth data
    local year_suffix=${birth_year: -2}
    local kvnr="${kvnr_prefix}${year_suffix}${birth_month}${birth_day}$(printf "%02d" $index)$(( (index * 7) % 10 ))"
    
    # Insurance type based on company
    local insurance_type="STATUTORY"
    if [[ "$insurance" == "Debeka" || "$insurance" == "HUK-COBURG" || "$insurance" == "Signal Iduna" ]]; then
        insurance_type="PRIVATE"
    fi
    
    echo "Creating: $first_name $last_name ($gender, $birth_date, $kvnr)"
    
    curl -s -X POST http://localhost:8080/api/v1/patients \
        -H "Content-Type: application/json" \
        -H "Authorization: Basic $(echo -n 'admin:dev-password' | base64)" \
        -d "{
            \"firstName\": \"$first_name\",
            \"lastName\": \"$last_name\", 
            \"birthDate\": \"$birth_date\",
            \"gender\": \"$gender\",
            \"kvnr\": \"$kvnr\",
            \"insuranceType\": \"$insurance_type\",
            \"insuranceCompanyName\": \"$insurance\",
            \"phone\": \"+49 $((100 + RANDOM % 900)) $((1000000 + RANDOM % 9000000))\",
            \"email\": \"$(echo $first_name | tr '[:upper:]' '[:lower:]').$(echo $last_name | tr '[:upper:]' '[:lower:]')@example.com\",
            \"consentCommunication\": $([[ $((RANDOM % 2)) -eq 0 ]] && echo true || echo false),
            \"consentDataProcessing\": true
        }" | jq '{id, firstName, lastName, kvnr, insuranceType, insuranceCompanyName}' 2>/dev/null
    
    echo ""
}

# Create 10 realistic patients
for i in {1..10}; do
    if [ $((i % 2)) -eq 0 ]; then
        create_realistic_patient $i "FEMALE"
    else 
        create_realistic_patient $i "MALE"
    fi
    sleep 1  # Rate limiting
done

echo "✅ Realistic test data creation completed!"