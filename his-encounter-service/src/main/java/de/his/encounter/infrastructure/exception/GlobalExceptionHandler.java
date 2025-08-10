package de.his.encounter.infrastructure.exception;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Erweiterte Global Exception Handler für den Encounter Service
 * 
 * Behandelt alle relevanten Exception-Typen für eine Healthcare-Anwendung:
 * - Application-spezifische Exceptions
 * - Validation Errors
 * - Feign Client Communication Errors
 * - Database Errors
 * - HTTP/Web Errors
 * - Security Errors
 * - Performance/Timeout Errors
 * 
 * @author HIS Development Team
 * @version 3.0 - Enhanced Healthcare Exception Handling
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

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
    // NEUE HEALTHCARE-SPEZIFISCHE EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(BusinessRuleViolationException ex) {
        logger.warn("Business rule violation: {} - {}", ex.getRuleCode(), ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                ex.getRuleCode(), // Verwendet den spezifischen Rule Code!
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EncounterAlreadyStartedException.class)
    public ResponseEntity<ErrorResponse> handleEncounterAlreadyStarted(EncounterAlreadyStartedException ex) {
        logger.warn("Encounter already started: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "ENCOUNTER_ALREADY_STARTED",
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EncounterAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponse> handleEncounterAlreadyCompleted(EncounterAlreadyCompletedException ex) {
        logger.warn("Encounter already completed: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "ENCOUNTER_ALREADY_COMPLETED",
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DocumentationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentationNotFound(DocumentationNotFoundException ex) {
        logger.warn("Documentation not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "DOCUMENTATION_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedEncounterAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedEncounterAccess(UnauthorizedEncounterAccessException ex) {
        logger.warn("Unauthorized encounter access attempt: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "UNAUTHORIZED_ENCOUNTER_ACCESS",
                "Access denied to this encounter",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // =========================================================================
    // VALIDATION EXCEPTIONS - ERWEITERT
    // =========================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        logger.debug("Validation error in request: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed for request fields",
                LocalDateTime.now(),
                errors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        logger.debug("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse(
                "CONSTRAINT_VIOLATION",
                "Request violates business constraints",
                LocalDateTime.now(),
                errors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.debug("Malformed JSON request: {}", ex.getMessage());

        String message = "Invalid JSON format in request body";
        if (ex.getMessage().contains("JSON parse error")) {
            message = "JSON parsing failed - check request format";
        }

        ErrorResponse error = new ErrorResponse(
                "MALFORMED_JSON",
                message,
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logger.debug("Type mismatch in request parameter: {}", ex.getMessage());

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

        ErrorResponse error = new ErrorResponse(
                "TYPE_MISMATCH",
                message,
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        logger.debug("Missing required parameter: {}", ex.getMessage());

        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

        ErrorResponse error = new ErrorResponse(
                "MISSING_PARAMETER",
                message,
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // FEIGN CLIENT EXCEPTIONS - ERWEITERT
    // =========================================================================

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleFeignNotFound(FeignException.NotFound ex) {
        logger.warn("Feign client 404 error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "EXTERNAL_RESOURCE_NOT_FOUND",
                "Referenced external resource not found",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<ErrorResponse> handleFeignUnauthorized(FeignException.Unauthorized ex) {
        logger.error("Feign client authentication failed: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "EXTERNAL_SERVICE_AUTH_FAILED",
                "Authentication with external service failed",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(FeignException.ServiceUnavailable.class)
    public ResponseEntity<ErrorResponse> handleFeignServiceUnavailable(FeignException.ServiceUnavailable ex) {
        logger.error("External service unavailable: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "EXTERNAL_SERVICE_UNAVAILABLE",
                "External service is temporarily unavailable",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(FeignException.InternalServerError.class)
    public ResponseEntity<ErrorResponse> handleFeignInternalError(FeignException.InternalServerError ex) {
        logger.error("External service internal error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "EXTERNAL_SERVICE_ERROR",
                "External service encountered an internal error",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleGenericFeignException(FeignException ex) {
        logger.error("Feign client error: Status {}, Message: {}", ex.status(), ex.getMessage());

        String errorCode = "EXTERNAL_SERVICE_ERROR";
        String message = "Communication with external service failed";
        HttpStatus status = HttpStatus.BAD_GATEWAY;

        // Spezifische Behandlung basierend auf HTTP Status
        if (ex.status() == 400) {
            errorCode = "EXTERNAL_SERVICE_BAD_REQUEST";
            message = "Invalid request to external service";
            status = HttpStatus.BAD_REQUEST;
        } else if (ex.status() == 403) {
            errorCode = "EXTERNAL_SERVICE_FORBIDDEN";
            message = "Access forbidden to external service";
            status = HttpStatus.FORBIDDEN;
        } else if (ex.status() >= 500) {
            errorCode = "EXTERNAL_SERVICE_UNAVAILABLE";
            message = "External service is currently unavailable";
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }

        ErrorResponse error = new ErrorResponse(errorCode, message, LocalDateTime.now());
        return new ResponseEntity<>(error, status);
    }

    // =========================================================================
    // DATABASE EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity constraint violated";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique") || ex.getMessage().contains("UNIQUE")) {
                message = "Duplicate entry - record already exists";
            } else if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("FOREIGN KEY")) {
                message = "Referenced record does not exist";
            } else if (ex.getMessage().contains("not null") || ex.getMessage().contains("NOT NULL")) {
                message = "Required field cannot be empty";
            }
        }

        ErrorResponse error = new ErrorResponse(
                "DATA_INTEGRITY_VIOLATION",
                message,
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex) {
        logger.error("Database SQL error: Code {}, State {}, Message: {}",
                ex.getErrorCode(), ex.getSQLState(), ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "DATABASE_ERROR",
                "Database operation failed",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        logger.error("Database access error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "DATABASE_ACCESS_ERROR",
                "Database is temporarily unavailable",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // =========================================================================
    // PERFORMANCE & TIMEOUT EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(TimeoutException ex) {
        logger.error("Operation timeout: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "OPERATION_TIMEOUT",
                "Operation took too long to complete",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.REQUEST_TIMEOUT);
    }

    // =========================================================================
    // HTTP/WEB EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        logger.debug("Unsupported media type: {}", ex.getMessage());

        String message = String.format("Media type '%s' not supported. Supported types: %s",
                ex.getContentType(), ex.getSupportedMediaTypes());

        ErrorResponse error = new ErrorResponse(
                "UNSUPPORTED_MEDIA_TYPE",
                message,
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        logger.debug("Resource not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                "The requested resource was not found",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        logger.debug("HTTP method not supported: {}", ex.getMessage());

        String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(), ex.getSupportedMethods());

        ErrorResponse error = new ErrorResponse(
                "METHOD_NOT_ALLOWED",
                message,
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // =========================================================================
    // RUNTIME EXCEPTIONS - VERFEINERT
    // =========================================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // Spezifische Behandlung für bekannte RuntimeException-Muster
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
                "A service error occurred while processing your request",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================================
    // FALLBACK EXCEPTION HANDLER
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getClass().getSimpleName(), ex);

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred while processing your request",
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
        private String path;
        private String correlationId;

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

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }
    }
}