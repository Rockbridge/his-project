package de.his.gateway.config;

import de.his.gateway.filter.JwtAuthenticationFilter;
import de.his.gateway.service.JwtService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final PvsGatewayProperties gatewayProperties; // ← Geändert

    public SecurityConfig(PvsGatewayProperties gatewayProperties) { // ← Geändert
        this.gatewayProperties = gatewayProperties;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))

                .authorizeExchange(exchanges -> {
                    if (gatewayProperties.getSecurity().getJwt().isEnabled()) {
                        exchanges
                                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                                .pathMatchers("/swagger-ui/**", "/api-docs/**", "/docs/**").permitAll()
                                .pathMatchers("/auth/**", "/login", "/logout").permitAll()
                                .pathMatchers("/fallback/**").permitAll()
                                .anyExchange().authenticated();
                    } else {
                        exchanges.anyExchange().permitAll();
                    }
                })

                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "his.gateway.security.jwt.enabled", havingValue = "true")
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    @ConditionalOnProperty(name = "his.gateway.security.jwt.enabled", havingValue = "false", matchIfMissing = true)
    public JwtAuthenticationFilter noOpJwtFilter() {
        return new JwtAuthenticationFilter(null);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Cache-Control", "Content-Type",
                "X-Requested-With", "Accept", "Origin",
                "X-Session-Token", "X-CSRF-Token"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
