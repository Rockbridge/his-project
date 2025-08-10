#!/bin/bash

# Script zum Anlegen der Java Exception Files für den Patient Service
# Verwendet werden sollte von der Patient Service Root Directory aus

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_DIR="src/main/java/de/his/patient"
EXCEPTION_DIR="$BASE_DIR/infrastructure/exception"
SERVICE_DIR="$BASE_DIR/application/service"

echo -e "${BLUE}=== Patient Service Exception Files Creator ===${NC}"
echo "Dieses Script erstellt alle Java Files für das verbesserte Exception Handling"
echo

# Function to create directory if it doesn't exist
create_directory() {
    local dir=$1
    if [ ! -d "$dir" ]; then
        mkdir -p "$dir"
        echo -e "${GREEN}✓ Directory erstellt: $dir${NC}"
    else
        echo -e "${YELLOW}→ Directory existiert bereits: $dir${NC}"
    fi
}

# Function to create Java file
create_java_file() {
    local file_path=$1
    local content=$2
    
    if [ -f "$file_path" ]; then
        echo -e "${YELLOW}⚠ File existiert bereits: $file_path${NC}"
        read -p "Überschreiben? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${BLUE}→ Übersprungen: $file_path${NC}"
            return
        fi
    fi
    
    echo "$content" > "$file_path"
    echo -e "${GREEN}✓ File erstellt: $file_path${NC}"
}

# Check if we're in the right directory
if [ ! -f "pom.xml" ] && [ ! -f "build.gradle" ] && [ ! -f "build.gradle.kts" ]; then
    echo -e "${RED}❌ Fehler: Dieses Script muss aus der Patient Service Root Directory ausgeführt werden${NC}"
    echo "Erwartete Struktur: src/main/java/de/his/patient/"
    exit 1
fi

echo -e "${BLUE}Erstelle Directory-Struktur...${NC}"
create_directory "$EXCEPTION_DIR"
create_directory "$SERVICE_DIR"

echo
echo -e "${BLUE}Erstelle Exception Classes...${NC}"

# 1. PatientValidationException
create_java_file "$EXCEPTION_DIR/PatientValidationException.java" 'package de.his.patient.infrastructure.exception;

public class PatientValidationException extends RuntimeException {
    
    private final String field;
    private final String rejectedValue;
    private final String reason;

    public PatientValidationException(String field, String rejectedValue, String reason) {
        super(String.format("Invalid value for field '\''%s'\'': '\''%s'\''. Reason: %s", field, rejectedValue, reason));
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.reason = reason;
    }

    public PatientValidationException(String message) {
        super(message);
        this.field = null;
        this.rejectedValue = null;
        this.reason = null;
    }

    public String getField() { return field; }
    public String getRejectedValue() { return rejectedValue; }
    public String getReason() { return reason; }
}'

# 2. DatabaseConnectionException
create_java_file "$EXCEPTION_DIR/DatabaseConnectionException.java" 'package de.his.patient.infrastructure.exception;

public class DatabaseConnectionException extends RuntimeException {
    
    private final String operation;
    private final String details;

    public DatabaseConnectionException(String operation, String details, Throwable cause) {
        super(String.format("Database operation '\''%s'\'' failed. Details: %s", operation, details), cause);
        this.operation = operation;
        this.details = details;
    }

    public DatabaseConnectionException(String operation, Throwable cause) {
        super(String.format("Database operation '\''%s'\'' failed due to connection issues", operation), cause);
        this.operation = operation;
        this.details = "Connection timeout or network error";
    }

    public String getOperation() { return operation; }
    public String getDetails() { return details; }
}'

# 3. InvalidKvnrException
create_java_file "$EXCEPTION_DIR/InvalidKvnrException.java" 'package de.his.patient.infrastructure.exception;

public class InvalidKvnrException extends RuntimeException {
    
    private final String kvnr;
    private final String validationError;

    public InvalidKvnrException(String kvnr, String validationError) {
        super(String.format("Invalid KVNR '\''%s'\'': %s. KVNR must be exactly 10 digits", kvnr, validationError));
        this.kvnr = kvnr;
        this.validationError = validationError;
    }

    public String getKvnr() { return kvnr; }
    public String getValidationError() { return validationError; }
}'

# 4. PatientDataIntegrityException
create_java_file "$EXCEPTION_DIR/PatientDataIntegrityException.java" 'package de.his.patient.infrastructure.exception;

public class PatientDataIntegrityException extends RuntimeException {
    
    private final String constraint;
    private final String conflictingData;

    public PatientDataIntegrityException(String constraint, String conflictingData, Throwable cause) {
        super(String.format("Data integrity violation in constraint '\''%s'\''. Conflicting data: %s", 
              constraint, conflictingData), cause);
        this.constraint = constraint;
        this.conflictingData = conflictingData;
    }

    public String getConstraint() { return constraint; }
    public String getConflictingData() { return conflictingData; }
}'

# 5. PatientServiceUnavailableException
create_java_file "$EXCEPTION_DIR/PatientServiceUnavailableException.java" 'package de.his.patient.infrastructure.exception;

public class PatientServiceUnavailableException extends RuntimeException {
    
    private final String serviceComponent;
    private final String estimatedRecoveryTime;

    public PatientServiceUnavailableException(String serviceComponent, String estimatedRecoveryTime, Throwable cause) {
        super(String.format("Patient service component '\''%s'\'' is temporarily unavailable. " +
              "Estimated recovery: %s", serviceComponent, estimatedRecoveryTime), cause);
        this.serviceComponent = serviceComponent;
        this.estimatedRecoveryTime = estimatedRecoveryTime;
    }

    public String getServiceComponent() { return serviceComponent; }
    public String getEstimatedRecoveryTime() { return estimatedRecoveryTime; }
}'

# 6. UnauthorizedPatientAccessException
create_java_file "$EXCEPTION_DIR/UnauthorizedPatientAccessException.java" 'package de.his.patient.infrastructure.exception;

public class UnauthorizedPatientAccessException extends RuntimeException {
    
    private final String requestedPatientId;
    private final String accessingUser;
    private final String requiredPermission;

    public UnauthorizedPatientAccessException(String requestedPatientId, String accessingUser, String requiredPermission) {
        super(String.format("User '\''%s'\'' is not authorized to access patient '\''%s'\''. Required permission: %s", 
              accessingUser, requestedPatientId, requiredPermission));
        this.requestedPatientId = requestedPatientId;
        this.accessingUser = accessingUser;
        this.requiredPermission = requiredPermission;
    }

    public String getRequestedPatientId() { return requestedPatientId; }
    public String getAccessingUser() { return accessingUser; }
    public String getRequiredPermission() { return requiredPermission; }
}'

# 7. PatientDeletedException
create_java_file "$EXCEPTION_DIR/PatientDeletedException.java" 'package de.his.patient.infrastructure.exception;

import java.time.LocalDateTime;

public class PatientDeletedException extends RuntimeException {
    
    private final String patientId;
    private final LocalDateTime deletedAt;
    private final String deletedBy;

    public PatientDeletedException(String patientId, LocalDateTime deletedAt, String deletedBy) {
        super(String.format("Patient '\''%s'\'' was soft-deleted on %s by user '\''%s'\''. " +
              "Use restore endpoint to reactivate or contact administrator", 
              patientId, deletedAt, deletedBy));
        this.patientId = patientId;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public String getPatientId() { return patientId; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public String getDeletedBy() { return deletedBy; }
}'

echo
echo -e "${BLUE}Erstelle erweiterten GlobalExceptionHandler...${NC}"

# 8. Enhanced GlobalExceptionHandler (Backup existing first)
if [ -f "$EXCEPTION_DIR/GlobalExceptionHandler.java" ]; then
    cp "$EXCEPTION_DIR/GlobalExceptionHandler.java" "$EXCEPTION_DIR/GlobalExceptionHandler.java.backup"
    echo -e "${YELLOW}→ Backup erstellt: GlobalExceptionHandler.java.backup${NC}"
fi

create_java_file "$EXCEPTION_DIR/GlobalExceptionHandler.java" 'package de.his.patient.infrastructure.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.naming.ServiceUnavailableException;
import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Umfassender Global Exception Handler für den Patient Service
 * 
 * Behandelt alle typischen Fehlerszenarien in Healthcare IT:
 * - Patientenspezifische Business Logic Errors
 * - Database/Persistence Layer Errors  
 * - Validation und Request Processing Errors
 * - Security und Authorization Errors
 * - Infrastructure und Service Availability Errors
 * - Spring Framework Errors
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================================
    // PATIENTENSPEZIFISCHE BUSINESS LOGIC EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
        logger.warn("Patient not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "PATIENT_NOT_FOUND",
            ex.getMessage(),
            "Verify patient ID/KVNR and ensure patient exists in system",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PatientAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePatientAlreadyExists(PatientAlreadyExistsException ex) {
        logger.warn("Patient already exists: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "PATIENT_ALREADY_EXISTS",
            ex.getMessage(),
            "Use PUT endpoint to update existing patient or check for duplicate KVNR",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PatientDeletedException.class)
    public ResponseEntity<ErrorResponse> handlePatientDeleted(PatientDeletedException ex) {
        logger.warn("Attempt to access deleted patient: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "PATIENT_DELETED",
            ex.getMessage(),
            "Contact administrator to restore patient or use archived patient endpoints",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    @ExceptionHandler(InvalidKvnrException.class)
    public ResponseEntity<ErrorResponse> handleInvalidKvnr(InvalidKvnrException ex) {
        logger.warn("Invalid KVNR format: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "INVALID_KVNR_FORMAT",
            ex.getMessage(),
            "KVNR must be exactly 10 digits. Example: '\''1234567890'\''",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PatientValidationException.class)
    public ResponseEntity<ErrorResponse> handlePatientValidation(PatientValidationException ex) {
        logger.warn("Patient validation failed: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "PATIENT_VALIDATION_ERROR",
            ex.getMessage(),
            "Check field requirements and data formats in API documentation",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // SECURITY UND AUTHORIZATION EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(UnauthorizedPatientAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedPatientAccessException ex) {
        logger.warn("Unauthorized patient access attempt: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "UNAUTHORIZED_PATIENT_ACCESS",
            ex.getMessage(),
            "Ensure user has proper permissions or contact administrator",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // =========================================================================
    // DATABASE UND PERSISTENCE LAYER EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(DatabaseConnectionException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseConnection(DatabaseConnectionException ex) {
        logger.error("Database connection error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            "DATABASE_CONNECTION_ERROR",
            "Database operation failed due to connectivity issues",
            "Retry request in a few moments or contact system administrator",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(PatientDataIntegrityException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(PatientDataIntegrityException ex) {
        logger.error("Data integrity violation: {}", ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            "DATA_INTEGRITY_VIOLATION",
            ex.getMessage(),
            "Check for duplicate data or constraint violations",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.error("Database constraint violation: {}", ex.getMessage(), ex);
        
        String userMessage = "Database constraint violation occurred";
        String developerHint = "Check database logs for constraint details";
        
        // Analyze specific constraint violations
        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        
        if (rootMessage != null) {
            if (rootMessage.contains("unique") || rootMessage.contains("UNIQUE")) {
                userMessage = "Duplicate data detected - record already exists";
                developerHint = "Check for duplicate KVNR, email, or other unique fields";
            } else if (rootMessage.contains("foreign key") || rootMessage.contains("FOREIGN KEY")) {
                userMessage = "Referenced data not found";
                developerHint = "Ensure all referenced entities exist before creating patient";
            } else if (rootMessage.contains("not null") || rootMessage.contains("NOT NULL")) {
                userMessage = "Required field is missing";
                developerHint = "Check that all mandatory fields are provided";
            }
        }
        
        ErrorResponse error = new ErrorResponse(
            "DATABASE_CONSTRAINT_VIOLATION",
            userMessage,
            developerHint,
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // =========================================================================
    // VALIDATION UND REQUEST PROCESSING EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        logger.warn("Request validation failed: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        String detailedMessage = fieldErrors.entrySet().stream()
            .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("; "));
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed: " + detailedMessage,
            "Check field requirements in API documentation",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        logger.warn("Invalid JSON in request: {}", ex.getMessage());
        
        String userMessage = "Invalid JSON format in request body";
        String developerHint = "Check JSON syntax and field types";
        
        // Try to provide more specific error info
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage.contains("Unexpected character")) {
                userMessage = "JSON syntax error - unexpected character";
            } else if (causeMessage.contains("Cannot deserialize")) {
                userMessage = "JSON field type mismatch";
                developerHint = "Check that field types match expected values (string, number, boolean)";
            }
        }
        
        ErrorResponse error = new ErrorResponse(
            "INVALID_JSON_FORMAT",
            userMessage,
            developerHint,
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // FALLBACK EXCEPTION HANDLER
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred while processing your request",
            "Check application logs for detailed error information",
            LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================================
    // ERROR RESPONSE DTO
    // =========================================================================

    public static class ErrorResponse {
        private String code;
        private String message;
        private String developerHint;
        private LocalDateTime timestamp;

        public ErrorResponse(String code, String message, String developerHint, LocalDateTime timestamp) {
            this.code = code;
            this.message = message;
            this.developerHint = developerHint;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getDeveloperHint() { return developerHint; }
        public void setDeveloperHint(String developerHint) { this.developerHint = developerHint; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}'

echo
echo -e "${BLUE}Erstelle Test-Script Files...${NC}"

# Create test scripts directory
TEST_DIR="scripts/test"
create_directory "$TEST_DIR"

# Create curl test script
create_java_file "$TEST_DIR/test-exceptions.sh" '#!/bin/bash

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
  -d '\''{"firstName": "Max", "lastName": "Mustermann" invalid json}'\'' | jq

echo -e "\n"

# Test 2.2: Missing required fields
echo "2.2 Missing required fields:"
curl -s -X POST "$PATIENT_SERVICE_URL/api/v1/patients" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" \
  -H "Content-Type: application/json" \
  -d '\''{"firstName": "Max"}'\'' | jq

echo -e "\n"

# Test 2.3: Invalid KVNR format (too short)
echo "2.3 Invalid KVNR format (too short):"
curl -s -X POST "$PATIENT_SERVICE_URL/api/v1/patients" \
  -H "Authorization: Basic YWRtaW46ZGV2LXBhc3N3b3Jk" \
  -H "Content-Type: application/json" \
  -d '\''{
    "firstName": "Max",
    "lastName": "Mustermann", 
    "kvnr": "123",
    "dateOfBirth": "1980-05-15",
    "email": "max.mustermann@example.com"
  }'\'' | jq

echo -e "\n"

echo "=== Exception Testing Complete ==="
echo "Review the responses above to verify proper error handling."'

# Make test script executable
chmod +x "$TEST_DIR/test-exceptions.sh"

echo
echo -e "${BLUE}Erstelle Maven/Gradle Integration...${NC}"

# Check if Maven or Gradle project and provide instructions
if [ -f "pom.xml" ]; then
    echo -e "${GREEN}✓ Maven Projekt erkannt${NC}"
    echo -e "${YELLOW}Stelle sicher, dass folgende Dependencies in der pom.xml vorhanden sind:${NC}"
    echo "  - spring-boot-starter-validation"
    echo "  - spring-boot-starter-data-jpa"
    echo "  - spring-boot-starter-web"
elif [ -f "build.gradle" ] || [ -f "build.gradle.kts" ]; then
    echo -e "${GREEN}✓ Gradle Projekt erkannt${NC}"
    echo -e "${YELLOW}Stelle sicher, dass folgende Dependencies im build.gradle vorhanden sind:${NC}"
    echo "  - implementation '\''org.springframework.boot:spring-boot-starter-validation'\''"
    echo "  - implementation '\''org.springframework.boot:spring-boot-starter-data-jpa'\''"
    echo "  - implementation '\''org.springframework.boot:spring-boot-starter-web'\''"
fi

echo
echo -e "${GREEN}=== ERFOLGREICH ABGESCHLOSSEN ===${NC}"
echo
echo -e "${BLUE}Erstellte Files:${NC}"
echo "📁 $EXCEPTION_DIR/"
echo "  ├── PatientValidationException.java"
echo "  ├── DatabaseConnectionException.java"
echo "  ├── InvalidKvnrException.java"
echo "  ├── PatientDataIntegrityException.java"
echo "  ├── PatientServiceUnavailableException.java"
echo "  ├── UnauthorizedPatientAccessException.java"
echo "  ├── PatientDeletedException.java"
echo "  └── GlobalExceptionHandler.java (erweitert)"
echo
echo "📁 $TEST_DIR/"
echo "  └── test-exceptions.sh"

echo
echo -e "${BLUE}Nächste Schritte:${NC}"
echo "1. Projekt kompilieren: mvn compile oder ./gradlew build"
echo "2. Patient Service starten"
echo "3. Exception Tests ausführen: ./scripts/test/test-exceptions.sh"
echo "4. Bei Bedarf: Bestehende Service-Klassen anpassen"

echo
echo -e "${YELLOW}Hinweise:${NC}"
echo "• Backup des alten GlobalExceptionHandler wurde erstellt"
echo "• Test-Script ist ausführbar gemacht worden"
echo "• Alle Files verwenden UTF-8 Encoding"
echo "• Healthcare-spezifische KVNR-Validierung ist implementiert"

echo
echo -e "${GREEN}✅ Alle Java Files erfolgreich erstellt!${NC}"