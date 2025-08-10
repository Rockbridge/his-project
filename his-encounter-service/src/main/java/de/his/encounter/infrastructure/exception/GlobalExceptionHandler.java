package de.his.encounter.infrastructure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler für den Encounter Service
 * 
 * WICHTIG: Behandelt nur application-spezifische Exceptions!
 * Spring Framework Exceptions werden bewusst NICHT abgefangen,
 * damit sie ihre korrekten HTTP-Status-Codes behalten.
 * 
 * @author HIS Development Team
 * @version 2.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================================
    // APPLICATION-SPEZIFISCHE EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(EncounterNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEncounterNotFound(EncounterNotFoundException ex) {
        logger.warn("Encounter not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "ENCOUNTER_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
        logger.warn("Patient not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "PATIENT_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidEncounterStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEncounterState(InvalidEncounterStateException ex) {
        logger.warn("Invalid encounter state: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "INVALID_ENCOUNTER_STATE",
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // VALIDATION EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed for request",
                LocalDateTime.now(),
                errors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // FEIGN CLIENT EXCEPTIONS (Patient Service Communication)
    // =========================================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // Spezifische Behandlung für Patient Service Unavailable
        if (ex.getMessage() != null && ex.getMessage().contains("Patient service temporarily unavailable")) {
            logger.error("Patient service unavailable: {}", ex.getMessage());

            ErrorResponse error = new ErrorResponse(
                    "PATIENT_SERVICE_UNAVAILABLE",
                    ex.getMessage(),
                    LocalDateTime.now());

            return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
        }

        // Andere business-logic RuntimeExceptions
        logger.error("Runtime exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "SERVICE_ERROR",
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================================
    // SPRING FRAMEWORK EXCEPTIONS - EXPLICIT HANDLING
    // =========================================================================

    /**
     * Behandelt 404-Fehler für nicht gefundene Ressourcen explizit
     * (z.B. Actuator Endpoints, die als Static Resources behandelt werden)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        logger.debug("Resource not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                "The requested resource was not found",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Behandelt HTTP Method Not Allowed explizit
     * (z.B. GET auf POST-only Endpoints)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        logger.debug("HTTP method not supported: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "METHOD_NOT_ALLOWED",
                String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                        ex.getMethod(), ex.getSupportedMethods()),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // =========================================================================
    // FALLBACK - NUR FÜR ECHTE UNBEKANNTE EXCEPTIONS
    // =========================================================================

    /**
     * FALLBACK: Nur für echte unbekannte Exceptions
     * 
     * WICHTIG: Diese Methode sollte nur noch selten aufgerufen werden,
     * da wir die häufigsten Spring Framework Exceptions explizit behandeln.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        // Log nur echte unbekannte Exceptions als ERROR
        logger.error("Unexpected error occurred: {}", ex.getClass().getSimpleName(), ex);

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================================
    // ERROR RESPONSE DTO
    // =========================================================================

    public static class ErrorResponse {
        private String code;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, String> details;

        public ErrorResponse(String code, String message, LocalDateTime timestamp) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
        }

        public ErrorResponse(String code, String message, LocalDateTime timestamp, Map<String, String> details) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
            this.details = details;
        }

        // Getters and Setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public Map<String, String> getDetails() {
            return details;
        }

        public void setDetails(Map<String, String> details) {
            this.details = details;
        }
    }
}