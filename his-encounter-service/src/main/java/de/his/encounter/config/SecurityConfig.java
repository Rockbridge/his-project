package de.his.encounter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

/**
 * Security Configuration für den Encounter Service
 * 
 * Konfiguration:
 * - Actuator Health Check: Öffentlich zugänglich
 * - Actuator Management Endpoints: Basic Authentication (ADMIN-Role)
 * - API Endpoints: Öffentlich für Service-zu-Service Kommunikation
 * - Swagger UI: Öffentlich zugänglich für Entwicklung
 * 
 * @author HIS Development Team
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.user.name:admin}")
    private String adminUsername;

    @Value("${spring.security.user.password:dev-password}")
    private String adminPassword;

    /**
     * Hauptkonfiguration der Security Filter Chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deaktiviert für REST API
                .csrf(csrf -> csrf.disable())

                // Session Management: Stateless für REST API
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // Health Check: Öffentlich zugänglich
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                        // Andere Actuator Endpoints: ADMIN-Role erforderlich
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Swagger UI und API Docs: Öffentlich für Development
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // API Endpoints: Öffentlich für Service-zu-Service Kommunikation
                        .requestMatchers("/api/**").permitAll()

                        // Static Resources: Öffentlich
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // Alle anderen Requests: Authentifizierung erforderlich
                        .anyRequest().authenticated())

                // Basic Authentication für Actuator Management Endpoints
                .httpBasic(basic -> basic
                        .realmName("Encounter Service Management"));

        return http.build();
    }

    /**
     * Password Encoder - BCrypt für sichere Passwort-Verschlüsselung
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * In-Memory User Details Service für Development
     * 
     * Erstellt einen Admin-User für Actuator-Zugriff basierend auf
     * den Environment Variables oder application.yml Konfiguration
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}