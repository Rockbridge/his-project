package de.his.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback Controller for Circuit Breaker
 * 
 * Provides graceful degradation when microservices are unavailable.
 * Returns appropriate error responses with helpful information.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Generic fallback for any service
     */
    @GetMapping("/default")
    public ResponseEntity<Map<String, Object>> defaultFallback() {
        return createFallbackResponse(
                "SERVICE_UNAVAILABLE",
                "The requested service is currently unavailable. Please try again later.",
                "default");
    }

    /**
     * Patient Service fallback
     */
    @GetMapping("/patient-service")
    public ResponseEntity<Map<String, Object>> patientServiceFallback() {
        return createFallbackResponse(
                "PATIENT_SERVICE_UNAVAILABLE",
                "Patient service is currently unavailable. Patient data cannot be accessed at this time.",
                "patient-service");
    }

    /**
     * Encounter Service fallback
     */
    @GetMapping("/encounter-service")
    public ResponseEntity<Map<String, Object>> encounterServiceFallback() {
        return createFallbackResponse(
                "ENCOUNTER_SERVICE_UNAVAILABLE",
                "Encounter service is currently unavailable. Encounter data cannot be accessed at this time.",
                "encounter-service");
    }

    /**
     * Generic service fallback with service name
     */
    @GetMapping("/{serviceName}")
    public ResponseEntity<Map<String, Object>> serviceFallback(@PathVariable String serviceName) {
        return createFallbackResponse(
                "SERVICE_UNAVAILABLE",
                String.format("%s service is currently unavailable. Please try again later.",
                        capitalize(serviceName.replace("-", " "))),
                serviceName);
    }

    /**
     * Health check fallback - indicates partial system availability
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthFallback() {
        Map<String, Object> response = Map.of(
                "status", "DEGRADED",
                "timestamp", LocalDateTime.now(),
                "message", "Some services are experiencing issues",
                "details", Map.of(
                        "gateway", "UP",
                        "downstream-services", "PARTIAL"));

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Creates a standardized fallback response
     */
    private ResponseEntity<Map<String, Object>> createFallbackResponse(
            String errorCode, String message, String serviceName) {

        Map<String, Object> response = Map.of(
                "error", Map.of(
                        "code", errorCode,
                        "message", message,
                        "service", serviceName,
                        "timestamp", LocalDateTime.now(),
                        "type", "CircuitBreakerFallback"),
                "status", "SERVICE_UNAVAILABLE",
                "recommendations", Map.of(
                        "retry", "Please try again in a few moments",
                        "alternative", "Check service status at /actuator/health",
                        "support", "Contact system administrator if problem persists"));

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Utility method to capitalize strings
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}