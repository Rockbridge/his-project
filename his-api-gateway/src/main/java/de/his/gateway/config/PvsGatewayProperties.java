package de.his.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Configuration Properties for HIS API Gateway
 * 
 * Centralizes all gateway-specific configuration in a type-safe manner.
 */
@Component
@ConfigurationProperties(prefix = "his.gateway")
public class PvsGatewayProperties {

    @NestedConfigurationProperty
    private SecurityConfig security = new SecurityConfig();

    @NestedConfigurationProperty
    private ServicesConfig services = new ServicesConfig();

    @NestedConfigurationProperty
    private RateLimitConfig rateLimit = new RateLimitConfig();

    @NestedConfigurationProperty
    private AuditConfig audit = new AuditConfig();

    // Getters and Setters
    public SecurityConfig getSecurity() {
        return security;
    }

    public void setSecurity(SecurityConfig security) {
        this.security = security;
    }

    public ServicesConfig getServices() {
        return services;
    }

    public void setServices(ServicesConfig services) {
        this.services = services;
    }

    public RateLimitConfig getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitConfig rateLimit) {
        this.rateLimit = rateLimit;
    }

    public AuditConfig getAudit() {
        return audit;
    }

    public void setAudit(AuditConfig audit) {
        this.audit = audit;
    }

    /**
     * Security Configuration
     */
    public static class SecurityConfig {
        @NestedConfigurationProperty
        private JwtConfig jwt = new JwtConfig();

        @NestedConfigurationProperty
        private RbacConfig rbac = new RbacConfig();

        public JwtConfig getJwt() {
            return jwt;
        }

        public void setJwt(JwtConfig jwt) {
            this.jwt = jwt;
        }

        public RbacConfig getRbac() {
            return rbac;
        }

        public void setRbac(RbacConfig rbac) {
            this.rbac = rbac;
        }
    }

    /**
     * JWT Configuration
     */
    public static class JwtConfig {
        private boolean enabled = false;
        private String secret = "default-secret-key-change-in-production";
        private long expiration = 86400; // 24 hours in seconds

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpiration() {
            return expiration;
        }

        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }
    }

    /**
     * RBAC Configuration
     */
    public static class RbacConfig {
        private boolean enabled = false;
        private List<String> adminRoles = List.of("ADMIN", "SYSTEM_ADMIN");
        private List<String> doctorRoles = List.of("DOCTOR", "PHYSICIAN");
        private List<String> nurseRoles = List.of("NURSE", "NURSE_PRACTITIONER");
        private List<String> readonlyRoles = List.of("READONLY", "GUEST");

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getAdminRoles() {
            return adminRoles;
        }

        public void setAdminRoles(List<String> adminRoles) {
            this.adminRoles = adminRoles;
        }

        public List<String> getDoctorRoles() {
            return doctorRoles;
        }

        public void setDoctorRoles(List<String> doctorRoles) {
            this.doctorRoles = doctorRoles;
        }

        public List<String> getNurseRoles() {
            return nurseRoles;
        }

        public void setNurseRoles(List<String> nurseRoles) {
            this.nurseRoles = nurseRoles;
        }

        public List<String> getReadonlyRoles() {
            return readonlyRoles;
        }

        public void setReadonlyRoles(List<String> readonlyRoles) {
            this.readonlyRoles = readonlyRoles;
        }
    }

    /**
     * Services Configuration
     */
    public static class ServicesConfig {
        @NestedConfigurationProperty
        private ServiceConfig patient = new ServiceConfig("http://patient-service:8081", Duration.ofSeconds(30), 3);

        @NestedConfigurationProperty
        private ServiceConfig encounter = new ServiceConfig("http://encounter-service:8082", Duration.ofSeconds(30), 3);

        public ServiceConfig getPatient() {
            return patient;
        }

        public void setPatient(ServiceConfig patient) {
            this.patient = patient;
        }

        public ServiceConfig getEncounter() {
            return encounter;
        }

        public void setEncounter(ServiceConfig encounter) {
            this.encounter = encounter;
        }
    }

    /**
     * Individual Service Configuration
     */
    public static class ServiceConfig {
        private String url;
        private Duration timeout;
        private int retries;

        public ServiceConfig() {
        }

        public ServiceConfig(String url, Duration timeout, int retries) {
            this.url = url;
            this.timeout = timeout;
            this.retries = retries;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public int getRetries() {
            return retries;
        }

        public void setRetries(int retries) {
            this.retries = retries;
        }
    }

    /**
     * Rate Limiting Configuration
     */
    public static class RateLimitConfig {
        private boolean enabled = false;
        private int defaultRate = 100; // requests per minute
        private int burstCapacity = 200;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getDefaultRate() {
            return defaultRate;
        }

        public void setDefaultRate(int defaultRate) {
            this.defaultRate = defaultRate;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }

    /**
     * Audit Configuration
     */
    public static class AuditConfig {
        private boolean enabled = true;
        private boolean includeRequestBody = false;
        private boolean includeResponseBody = false;
        private List<String> sensitiveHeaders = List.of("authorization", "x-api-key", "cookie");

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeRequestBody() {
            return includeRequestBody;
        }

        public void setIncludeRequestBody(boolean includeRequestBody) {
            this.includeRequestBody = includeRequestBody;
        }

        public boolean isIncludeResponseBody() {
            return includeResponseBody;
        }

        public void setIncludeResponseBody(boolean includeResponseBody) {
            this.includeResponseBody = includeResponseBody;
        }

        public List<String> getSensitiveHeaders() {
            return sensitiveHeaders;
        }

        public void setSensitiveHeaders(List<String> sensitiveHeaders) {
            this.sensitiveHeaders = sensitiveHeaders;
        }
    }
}
