package com.example.cadastro_usuario.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, @DefaultValue("3600") long expirationSeconds) {

    private static final int TAMANHO_MINIMO_SEGREDO_EM_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO_EM_BYTES) {
            throw new IllegalArgumentException(
                    "app.security.jwt.secret deve ter pelo menos 32 bytes (exigência do HS256)");
        }
    }
}
