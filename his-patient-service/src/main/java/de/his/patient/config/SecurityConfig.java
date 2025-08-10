package de.his.patient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions().sameOrigin()  // Erlaubt H2 Console
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/h2-console/**").permitAll()  // H2 Console erlauben
                .requestMatchers("/actuator/**").permitAll()    // Actuator erlauben
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger erlauben
                .anyRequest().permitAll()  // Temporary: Allow all requests
            );
            
        return http.build();
    }
}
