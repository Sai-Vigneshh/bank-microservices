package com.bank.auth.service;

import com.bank.auth.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    public String generateToken(User user){
        Map<String,Object> extraaClaims = new HashMap<>();
        extraaClaims.put("userId", user.getId());
        extraaClaims.put("role", user.getRole().name());
        return BuildToken(extraaClaims,user,jwtExpiration);
    }
    private String BuildToken(Map<String, Object> extraClaims,
                              User user,
                              long expiration){
        return  Jwts.builder()
                .addClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSignInKey())
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}