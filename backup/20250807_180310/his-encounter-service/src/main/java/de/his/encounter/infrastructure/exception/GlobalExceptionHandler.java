package de.his.encounter.infrastructure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

        // Andere RuntimeExceptions
        logger.error("Runtime exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "SERVICE_ERROR",
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        logger.error("Unexpected error occurred", ex);

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Error Response DTO
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
