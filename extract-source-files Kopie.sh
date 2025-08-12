# In das Projekt-Root-Verzeichnis wechseln (wo his-encounter-service/, his-patient-service/, his-api-gateway/ liegen)
cd his-project  # oder wie auch immer dein Root-Verzeichnis heißt

# Sources Verzeichnis erstellen/leeren
echo "Preparing Sources directory..."
if [ -d "Sources" ]; then
    rm -rf Sources/*
else
    mkdir Sources
fi

# Funktion für Service-Extraktion
extract_service() {
    local service_name=$1
    local service_dir=$2
    local output_file="${service_name}-source.txt"
    
    echo "Extracting ${service_name}..."
    
    # In Service-Verzeichnis wechseln
    cd "$service_dir"
    
    # Output-File erstellen
    echo "# HIS ${service_name} - Complete Source Code Structure" > "$output_file"
    echo "Generated: $(date)" >> "$output_file"
    echo "Project: Hospital Information System (HIS) - ${service_name}" >> "$output_file"
    echo "=========================================================" >> "$output_file"
    echo "" >> "$output_file"
    
    # Verzeichnisstruktur anzeigen (ohne Backups)
    echo "## Directory Structure" >> "$output_file"
    echo "\`\`\`" >> "$output_file"
    find . -type f -name "*.java" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" -o -name "*.xml" -o -name "*.sql" -o -name "Dockerfile" -o -name "*.md" | grep -v target/ | grep -v .git/ | grep -v backup | grep -v "\.bak" | grep -v "\.backup" | grep -v "~$" | sort >> "$output_file"
    echo "\`\`\`" >> "$output_file"
    echo "" >> "$output_file"
    
    # Tree-Struktur
    echo "## Tree Structure" >> "$output_file"
    echo "\`\`\`" >> "$output_file"
    if command -v tree >/dev/null 2>&1; then
        tree -I 'target|.git|*.class|*.jar|backup*|*.bak|*.backup|*~' >> "$output_file"
    else
        echo "tree command not available - using find alternative:" >> "$output_file"
        find . -type d | grep -v target/ | grep -v .git/ | grep -v backup | sort | sed 's/[^/]*\//|  /g; s/|  \([^|]\)/+--\1/' >> "$output_file"
    fi
    echo "\`\`\`" >> "$output_file"
    echo "" >> "$output_file"
    
    # Source Code Files
    echo "## Source Code Files" >> "$output_file"
    echo "" >> "$output_file"
    
    # Java Files
    find . -name "*.java" | grep -v target/ | grep -v backup | grep -v "\.bak" | grep -v "\.backup" | grep -v "~$" | sort | while read file; do
        echo "### File: $file" >> "$output_file"
        echo "\`\`\`java" >> "$output_file"
        cat "$file" >> "$output_file"
        echo "" >> "$output_file"
        echo "\`\`\`" >> "$output_file"
        echo "" >> "$output_file"
    done
    
    # Configuration Files
    for ext in yml yaml properties; do
        find . -name "*.$ext" | grep -v target/ | grep -v backup | grep -v "\.bak" | grep -v "\.backup" | grep -v "~$" | sort | while read file; do
            echo "### File: $file" >> "$output_file"
            echo "\`\`\`$ext" >> "$output_file"
            cat "$file" >> "$output_file"
            echo "" >> "$output_file"
            echo "\`\`\`" >> "$output_file"
            echo "" >> "$output_file"
        done
    done
    
    # XML Files
    find . -name "*.xml" | grep -v target/ | grep -v backup | grep -v "\.bak" | grep -v "\.backup" | grep -v "~$" | sort | while read file; do
        echo "### File: $file" >> "$output_file"
        echo "\`\`\`xml" >> "$output_file"
        cat "$file" >> "$output_file"
        echo "" >> "$output_file"
        echo "\`\`\`" >> "$output_file"
        echo "" >> "$output_file"
    done
    
    # SQL Files
    find . -name "*.sql" | grep -v target/ | grep -v backup | grep -v "\.bak" | grep -v "\.backup" | grep -v "~$" | sort | while read file; do
        echo "### File: $file" >> "$output_file"
        echo "\`\`\`sql" >> "$output_file"
        cat "$file" >> "$output_file"
        echo "" >> "$output_file"
        echo "\`\`\`" >> "$output_file"
        echo "" >> "$output_file"
    done
    
    # Dockerfile
    if [ -f "Dockerfile" ] && ! echo "Dockerfile" | grep -q -E "(backup|\.bak|\.backup|~$)"; then
        echo "### File: ./Dockerfile" >> "$output_file"
        echo "\`\`\`dockerfile" >> "$output_file"
        cat Dockerfile >> "$output_file"
        echo "" >> "$output_file"
        echo "\`\`\`" >> "$output_file"
        echo "" >> "$output_file"
    fi
    
    # Markdown Files
    find . -name "*.md" | grep -v target/ | grep -v backup | grep -v "\.bak" | grep -v "\.backup" | grep -v "~$" | sort | while read file; do
        echo "### File: $file" >> "$output_file"
        echo "\`\`\`markdown" >> "$output_file"
        cat "$file" >> "$output_file"
        echo "" >> "$output_file"
        echo "\`\`\`" >> "$output_file"
        echo "" >> "$output_file"
    done
    
    # File in Sources Verzeichnis kopieren
    cp "$output_file" "../Sources/"
    
    # Zurück zum Root-Verzeichnis
    cd ..
    
    echo "${service_name} extraction completed: Sources/${output_file}"
}

# Root Directory Files extrahieren
extract_root_files() {
    echo "Extracting Root Directory files..."
    
    local output_file="his-project-root-source.txt"
    
    # Output-File erstellen
    echo "# HIS Project Root - Configuration and Infrastructure Files" > "$output_file"
    echo "Generated: $(date)" >> "$output_file"
    echo "Project: Hospital Information System (HIS) - Root Directory" >> "$output_file"
    echo "=========================================================" >> "$output_file"
    echo "" >> "$output_file"
    
    # Root Directory Structure
    echo "## Root Directory Structure" >> "$output_file"
    echo "\`\`\`" >> "$output_file"
    find . -maxdepth 2 -type f -name "*.yml" -o -name "*.yaml" -o -name "*.md" -o -name "*.txt" -o -name "*.json" -o -name "*.xml" -o -name "*.properties" -o -name "Dockerfile" -o -name "*.sh" | grep -v backup | grep -v "\.bak" | grep -v "\.backup" | grep -v "~$" | grep -v "/target/" | grep -v "Sources/" | sort >> "$output_file"
    echo "\`\`\`" >> "$output_file"
    echo "" >> "$output_file"
    
    # Services Overview
    echo "## Available Services" >> "$output_file"
    echo "\`\`\`" >> "$output_file"
    ls -la | grep "^d" | grep -E "(his-|init-)" >> "$output_file"
    echo "\`\`\`" >> "$output_file"
    echo "" >> "$output_file"
    
    # Root Configuration Files
    echo "## Root Configuration Files" >> "$output_file"
    echo "" >> "$output_file"
    
    # Docker Compose Files
    for file in docker-compose*.yml docker-compose*.yaml; do
        if [ -f "$file" ] && ! echo "$file" | grep -q -E "(backup|\.bak|\.backup|~$)"; then
            echo "### File: ./$file" >> "$output_file"
            echo "\`\`\`yaml" >> "$output_file"
            cat "$file" >> "$output_file"
            echo "" >> "$output_file"
            echo "\`\`\`" >> "$output_file"
            echo "" >> "$output_file"
        fi
    done
    
    # README Files
    for file in README*.md readme*.md; do
        if [ -f "$file" ] && ! echo "$file" | grep -q -E "(backup|\.bak|\.backup|~$)"; then
            echo "### File: ./$file" >> "$output_file"
            echo "\`\`\`markdown" >> "$output_file"
            cat "$file" >> "$output_file"
            echo "" >> "$output_file"
            echo "\`\`\`" >> "$output_file"
            echo "" >> "$output_file"
        fi
    done
    
    # Other Configuration Files
    for file in *.json *.xml *.properties *.txt; do
        if [ -f "$file" ] && ! echo "$file" | grep -q -E "(backup|\.bak|\.backup|~$)" && ! echo "$file" | grep -q "source\.txt$"; then
            echo "### File: ./$file" >> "$output_file"
            echo "\`\`\`" >> "$output_file"
            cat "$file" >> "$output_file"
            echo "" >> "$output_file"
            echo "\`\`\`" >> "$output_file"
            echo "" >> "$output_file"
        fi
    done
    
    # Shell Scripts
    for file in *.sh; do
        if [ -f "$file" ] && ! echo "$file" | grep -q -E "(backup|\.bak|\.backup|~$)"; then
            echo "### File: ./$file" >> "$output_file"
            echo "\`\`\`bash" >> "$output_file"
            cat "$file" >> "$output_file"
            echo "" >> "$output_file"
            echo "\`\`\`" >> "$output_file"
            echo "" >> "$output_file"
        fi
    done
    
    # File in Sources Verzeichnis kopieren
    cp "$output_file" "Sources/"
    
    echo "Root directory extraction completed: Sources/$output_file"
}

# Hauptausführung
echo "Starting HIS Project Source Code Extraction..."
echo "=============================================="

# Root Files extrahieren
extract_root_files

# Init Scripts als separater Service extrahieren
if [ -d "init-scripts" ]; then
    extract_service "Init Scripts" "init-scripts"
else
    echo "Warning: init-scripts directory not found"
fi

# Services extrahieren (prüfen ob Verzeichnisse existieren)
if [ -d "his-encounter-service" ]; then
    extract_service "Encounter Service" "his-encounter-service"
else
    echo "Warning: his-encounter-service directory not found"
fi

if [ -d "his-patient-service" ]; then
    extract_service "Patient Service" "his-patient-service"
else
    echo "Warning: his-patient-service directory not found"
fi

if [ -d "his-api-gateway" ]; then
    extract_service "API Gateway" "his-api-gateway"
else
    echo "Warning: his-api-gateway directory not found"
fi

# Zusammenfassung erstellen
echo ""
echo "Creating extraction summary..."
summary_file="Sources/00-extraction-summary.txt"
echo "HIS Project Source Code Extraction Summary" > "$summary_file"
echo "Generated: $(date)" >> "$summary_file"
echo "==========================================" >> "$summary_file"
echo "" >> "$summary_file"
echo "Extracted Files:" >> "$summary_file"
ls -la Sources/ >> "$summary_file"
echo "" >> "$summary_file"
echo "File Sizes:" >> "$summary_file"
du -h Sources/* >> "$summary_file"

echo ""
echo "=============================================="
echo "Extraction Summary:"
echo "- All files are now available in: ./Sources/"
echo "- Root Directory: Sources/his-project-root-source.txt"
if [ -d "init-scripts" ]; then
    echo "- Init Scripts: Sources/Init Scripts-source.txt"
fi
if [ -d "his-encounter-service" ]; then
    echo "- Encounter Service: Sources/Encounter Service-source.txt"
fi
if [ -d "his-patient-service" ]; then
    echo "- Patient Service: Sources/Patient Service-source.txt"
fi
if [ -d "his-api-gateway" ]; then
    echo "- API Gateway: Sources/API Gateway-source.txt"
fi
echo "- Summary: Sources/00-extraction-summary.txt"
echo ""
echo "All source code extractions completed and copied to Sources directory!"