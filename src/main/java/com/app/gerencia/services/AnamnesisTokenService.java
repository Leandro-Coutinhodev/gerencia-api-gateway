package com.app.gerencia.services;

import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AnamnesisTokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public AnamnesisTokenService(JwtEncoder encoder, JwtDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public String generateToken(Long patientId, Long anamnesisId) {
        var claims = JwtClaimsSet.builder()
                .claim("patientId", patientId)
                .claim("anamnesisId", anamnesisId)
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt decodeToken(String token) {
        return this.decoder.decode(token);
    }
}
