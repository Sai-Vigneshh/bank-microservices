package com.bank.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private Key getSignKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public void validateToken(final String token) {
        Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token);
    }

    // Long లేదా Integer వచ్చినా సేఫ్‌గా String గా మారుస్తుంది
    public String extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        return userId != null ? String.valueOf(userId) : null;
    }

    // auth-service లో పెట్టిన 'role' క్లెయిమ్ (ఉదా: ROLE_CUSTOMER) ను రీడ్ చేస్తుంది
    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role != null ? String.valueOf(role) : null;
    }

    // ఇమెయిల్ సాధారణంగా JWT Subject లో ఉంటుంది
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }
}