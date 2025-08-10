package de.his.patient.infrastructure.exception;

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
            "KVNR must be exactly 10 digits. Example: '1234567890'",
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
}
