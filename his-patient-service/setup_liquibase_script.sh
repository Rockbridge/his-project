#!/bin/bash

# =============================================================================
# HIS Patient Service - Liquibase Setup Script
# =============================================================================
# Dieses Script:
# 1. Erstellt die benötigten Verzeichnisse für Liquibase
# 2. Kopiert die Liquibase XML-Dateien in die korrekten Verzeichnisse
# 3. Löscht die temporären Dateien aus dem Root-Verzeichnis
# 4. Führt eine Validierung durch
# =============================================================================

set -euo pipefail  # Exit bei Fehlern, undefined vars, pipe failures

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# =============================================================================
# Konfiguration
# =============================================================================

PROJECT_ROOT="."
PATIENT_SERVICE_DIR="./his-patient-service"
RESOURCES_DIR="${PATIENT_SERVICE_DIR}/src/main/resources"
CHANGELOG_DIR="${RESOURCES_DIR}/db/changelog"

# Dateien die kopiert werden sollen
declare -A FILES=(
    ["db.changelog-master.xml"]="${CHANGELOG_DIR}/db.changelog-master.xml"
    ["001-create-persons-table.xml"]="${CHANGELOG_DIR}/001-create-persons-table.xml"
    ["002-create-patients-table.xml"]="${CHANGELOG_DIR}/002-create-patients-table.xml"
    ["003-create-addresses-table.xml"]="${CHANGELOG_DIR}/003-create-addresses-table.xml"
    ["004-create-indexes.xml"]="${CHANGELOG_DIR}/004-create-indexes.xml"
    ["liquibase.properties"]="${RESOURCES_DIR}/liquibase.properties"
)

# =============================================================================
# Funktionen
# =============================================================================

check_prerequisites() {
    log_info "Überprüfe Voraussetzungen..."
    
    # Patient Service Verzeichnis prüfen
    if [[ ! -d "$PATIENT_SERVICE_DIR" ]]; then
        log_error "Patient Service Verzeichnis nicht gefunden: $PATIENT_SERVICE_DIR"
        log_info "Führe dieses Script aus dem HIS Projekt Root-Verzeichnis aus!"
        exit 1
    fi
    
    # Maven pom.xml prüfen
    if [[ ! -f "${PATIENT_SERVICE_DIR}/pom.xml" ]]; then
        log_error "Patient Service pom.xml nicht gefunden!"
        exit 1
    fi
    
    log_success "Voraussetzungen erfüllt"
}

create_directories() {
    log_info "Erstelle Verzeichnisstruktur..."
    
    # Hauptverzeichnisse erstellen
    mkdir -p "$RESOURCES_DIR"
    mkdir -p "$CHANGELOG_DIR"
    
    # Verzeichnisse prüfen
    for dir in "$RESOURCES_DIR" "$CHANGELOG_DIR"; do
        if [[ -d "$dir" ]]; then
            log_success "Verzeichnis erstellt: $dir"
        else
            log_error "Konnte Verzeichnis nicht erstellen: $dir"
            exit 1
        fi
    done
}

backup_existing_files() {
    log_info "Erstelle Backup existierender Dateien..."
    
    local backup_dir="${PATIENT_SERVICE_DIR}/backup-$(date +%Y%m%d-%H%M%S)"
    local backup_created=false
    
    for source_file in "${!FILES[@]}"; do
        local target_file="${FILES[$source_file]}"
        
        if [[ -f "$target_file" ]]; then
            if [[ ! -d "$backup_dir" ]]; then
                mkdir -p "$backup_dir"
                backup_created=true
                log_info "Backup-Verzeichnis erstellt: $backup_dir"
            fi
            
            cp "$target_file" "$backup_dir/"
            log_warning "Backup erstellt für: $(basename "$target_file")"
        fi
    done
    
    if [[ "$backup_created" == true ]]; then
        log_success "Backup abgeschlossen in: $backup_dir"
    else
        log_info "Keine existierenden Dateien zum Backup gefunden"
    fi
}

copy_liquibase_files() {
    log_info "Kopiere Liquibase-Dateien..."
    
    local copied_count=0
    local total_count=${#FILES[@]}
    
    for source_file in "${!FILES[@]}"; do
        local target_file="${FILES[$source_file]}"
        
        if [[ -f "$source_file" ]]; then
            cp "$source_file" "$target_file"
            log_success "Kopiert: $source_file → $target_file"
            ((copied_count++))
        else
            log_warning "Quelldatei nicht gefunden: $source_file"
        fi
    done
    
    log_info "Dateien kopiert: $copied_count/$total_count"
    
    if [[ $copied_count -eq 0 ]]; then
        log_error "Keine Dateien wurden kopiert!"
        log_info "Stelle sicher, dass die Liquibase XML-Dateien im aktuellen Verzeichnis liegen."
        exit 1
    fi
}

cleanup_source_files() {
    log_info "Lösche temporäre Dateien aus dem Root-Verzeichnis..."
    
    local deleted_count=0
    
    for source_file in "${!FILES[@]}"; do
        if [[ -f "$source_file" ]]; then
            rm "$source_file"
            log_success "Gelöscht: $source_file"
            ((deleted_count++))
        fi
    done
    
    log_info "Temporäre Dateien gelöscht: $deleted_count"
}

validate_setup() {
    log_info "Validiere Liquibase-Setup..."
    
    local validation_passed=true
    
    # Prüfe alle Zieldateien
    for source_file in "${!FILES[@]}"; do
        local target_file="${FILES[$source_file]}"
        
        if [[ -f "$target_file" ]]; then
            log_success "✓ Vorhanden: $(basename "$target_file")"
        else
            log_error "✗ Fehlt: $target_file"
            validation_passed=false
        fi
    done
    
    # Prüfe XML-Struktur der Master-Changelog
    local master_changelog="${CHANGELOG_DIR}/db.changelog-master.xml"
    if [[ -f "$master_changelog" ]]; then
        if command -v xmllint >/dev/null 2>&1; then
            if xmllint --noout "$master_changelog" 2>/dev/null; then
                log_success "✓ Master Changelog XML ist valide"
            else
                log_warning "⚠ Master Changelog XML-Validierung fehlgeschlagen"
            fi
        else
            log_info "xmllint nicht verfügbar - XML-Validierung übersprungen"
        fi
    fi
    
    if [[ "$validation_passed" == true ]]; then
        log_success "Validierung erfolgreich abgeschlossen!"
    else
        log_error "Validierung fehlgeschlagen!"
        exit 1
    fi
}

show_next_steps() {
    log_info "Setup abgeschlossen! Nächste Schritte:"
    echo
    echo "1. Überprüfe die application-docker-minimal.yml:"
    echo "   spring:"
    echo "     jpa:"
    echo "       hibernate:"
    echo "         ddl-auto: none  # ⚠️ WICHTIG: JPA deaktivieren!"
    echo "     liquibase:"
    echo "       enabled: true"
    echo
    echo "2. PostgreSQL Schema erstellen (falls nötig):"
    echo "   docker exec -it his-postgres-minimal psql -U his_user -d his_db"
    echo "   CREATE SCHEMA IF NOT EXISTS his_patient;"
    echo
    echo "3. Liquibase ausführen:"
    echo "   cd $PATIENT_SERVICE_DIR"
    echo "   mvn liquibase:update"
    echo
    echo "4. Service testen:"
    echo "   mvn spring-boot:run -Dspring-boot.run.profiles=docker-minimal"
    echo
    log_success "Alle Liquibase-Dateien wurden erfolgreich eingerichtet!"
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    echo "============================================================================="
    echo "🏥 HIS Patient Service - Liquibase Setup"
    echo "============================================================================="
    echo
    
    check_prerequisites
    create_directories
    backup_existing_files
    copy_liquibase_files
    cleanup_source_files
    validate_setup
    
    echo
    echo "============================================================================="
    show_next_steps
    echo "============================================================================="
}

# Script ausführen
main "$@"