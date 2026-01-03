package com.app.gerencia.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class AnamnesisTokenService {

    @Autowired
    @Qualifier("anamnesisSecretKey")
    private SecretKey secretKey;

    public String generateToken(Long patientId, Long anamnesisId) {
        return Jwts.builder()
                .claim("p", patientId)
                .claim("a", anamnesisId)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getPatientId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("p", Long.class);
    }

    public Long getAnamnesisId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("a", Long.class);
    }
}