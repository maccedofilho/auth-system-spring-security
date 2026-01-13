package com.macedo.auth.authsystem.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpirationMs;
    private long refreshTokenExpirationMs;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET is missing or too weak (min 32 chars).");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET is missing or too weak (min 32 chars).");
        }
    }
}


