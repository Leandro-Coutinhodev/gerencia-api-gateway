package com.app.gerencia.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class AnamnesisTokenConfig {

    @Bean(name = "anamnesisSecretKey")
    public SecretKey anamnesisSecretKey() {
        // Gera uma chave forte de 256 bits automaticamente
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }
}