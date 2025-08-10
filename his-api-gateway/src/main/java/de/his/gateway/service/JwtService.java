package de.his.gateway.service;

import de.his.gateway.config.PvsGatewayProperties; // ← Geändert
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Service for Token Generation and Validation
 */
@Service
@ConditionalOnProperty(name = "his.gateway.security.jwt.enabled", havingValue = "true")
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtService(PvsGatewayProperties gatewayProperties) { // ← Geändert
        String secret = gatewayProperties.getSecurity().getJwt().getSecret();
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationTime = gatewayProperties.getSecurity().getJwt().getExpiration();
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Instant now = Instant.now();
        Instant expiration = now.plus(expirationTime, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("iat", Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationTime, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("iat", Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get("roles"));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.resolve(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}
