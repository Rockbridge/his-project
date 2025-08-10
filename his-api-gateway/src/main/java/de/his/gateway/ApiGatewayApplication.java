package de.his.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

/**
 * Main Application Class for HIS API Gateway
 * 
 * Provides centralized routing, authentication, and cross-cutting concerns
 * for the Hospital Information System microservices.
 */
@SpringBootApplication
public class ApiGatewayApplication {

        public static void main(String[] args) {
                SpringApplication.run(ApiGatewayApplication.class, args);
        }

        /**
         * Configures routing rules for microservices
         * 
         * Routes:
         * - /api/v1/patients/** → Patient Service (8081)
         * - /api/v1/encounters/** → Encounter Service (8082)
         * - /actuator/** → Individual service health endpoints
         */
        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()

                                // Patient Service Routes
                                .route("patient-service", r -> r
                                                .path("/api/v1/patients/**")
                                                .filters(f -> f
                                                                .stripPrefix(0) // Keep full path
                                                                .addRequestHeader("X-Gateway-Request",
                                                                                "patient-service")
                                                                .addResponseHeader("X-Gateway-Response",
                                                                                "patient-service")
                                                                .circuitBreaker(config -> config
                                                                                .setName("patient-service-cb")
                                                                                .setFallbackUri("forward:/fallback/patient-service"))
                                                                .retry(config -> config
                                                                                .setRetries(3)
                                                                                .setBackoff(Duration.ofMillis(100),
                                                                                                Duration.ofMillis(1000),
                                                                                                2, true)))
                                                .uri("http://patient-service:8081"))

                                // Encounter Service Routes
                                .route("encounter-service", r -> r
                                                .path("/api/v1/encounters/**")
                                                .filters(f -> f
                                                                .stripPrefix(0) // Keep full path
                                                                .addRequestHeader("X-Gateway-Request",
                                                                                "encounter-service")
                                                                .addResponseHeader("X-Gateway-Response",
                                                                                "encounter-service")
                                                                .circuitBreaker(config -> config
                                                                                .setName("encounter-service-cb")
                                                                                .setFallbackUri("forward:/fallback/encounter-service"))
                                                                .retry(config -> config
                                                                                .setRetries(3)
                                                                                .setBackoff(Duration.ofMillis(100),
                                                                                                Duration.ofMillis(1000),
                                                                                                2, true)))
                                                .uri("http://encounter-service:8082"))

                                // Health Check Routes - Direct pass-through
                                .route("patient-health", r -> r
                                                .path("/services/patient/actuator/**")
                                                .filters(f -> f
                                                                .stripPrefix(2) // Remove /services/patient
                                                                .addResponseHeader("X-Health-Service",
                                                                                "patient-service"))
                                                .uri("http://patient-service:8081"))

                                .route("encounter-health", r -> r
                                                .path("/services/encounter/actuator/**")
                                                .filters(f -> f
                                                                .stripPrefix(2) // Remove /services/encounter
                                                                .addResponseHeader("X-Health-Service",
                                                                                "encounter-service"))
                                                .uri("http://encounter-service:8082"))

                                // API Documentation Routes
                                .route("patient-docs", r -> r
                                                .path("/docs/patient/**")
                                                .filters(f -> f
                                                                .stripPrefix(2) // Remove /docs/patient
                                                                .addResponseHeader("X-Docs-Service", "patient-service"))
                                                .uri("http://patient-service:8081"))

                                .route("encounter-docs", r -> r
                                                .path("/docs/encounter/**")
                                                .filters(f -> f
                                                                .stripPrefix(2) // Remove /docs/encounter
                                                                .addResponseHeader("X-Docs-Service",
                                                                                "encounter-service"))
                                                .uri("http://encounter-service:8082"))

                                .build();
        }
}