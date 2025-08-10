package de.his.gateway.filter;

import de.his.gateway.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for API Gateway
 * 
 * Intercepts requests and validates JWT tokens in the Authorization header.
 * Sets authentication context for downstream filters.
 */
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Skip JWT processing if service is null (JWT disabled)
        if (jwtService == null) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();

        // Skip JWT for public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token provided - let security config handle this
            return chain.filter(exchange);
        }

        try {
            String token = jwtService.extractTokenFromHeader(authHeader);

            if (token != null && jwtService.validateToken(token)) {
                // Extract user information from token
                String username = jwtService.extractUsername(token);
                List<String> roles = jwtService.extractRoles(token);

                // Convert roles to authorities
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                // Create authentication object
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                // Set authentication in security context and continue
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            } else {
                // Invalid token - return unauthorized
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

        } catch (Exception e) {
            // Token processing error - return unauthorized
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * Check if path is public and doesn't require authentication
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/docs/") ||
                path.startsWith("/auth/") ||
                path.startsWith("/login") ||
                path.startsWith("/logout") ||
                path.startsWith("/fallback/");
    }
}