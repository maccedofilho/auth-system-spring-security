package com.macedo.auth.authsystem.security;

import com.macedo.auth.authsystem.config.JwtProperties;
import com.macedo.auth.authsystem.service.JwtBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final JwtBlacklistService blacklistService;

    public JwtTokenProvider(JwtProperties jwtProperties, JwtBlacklistService blacklistService) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.accessTokenExpirationMs = jwtProperties.getAccessTokenExpirationMs();
        this.refreshTokenExpirationMs = jwtProperties.getRefreshTokenExpirationMs();
        this.blacklistService = blacklistService;
    }

    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setId(jti)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setId(jti)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public String getTokenId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getId();
    }

    public void blacklistToken(String token) {
        String jti = getTokenId(token);
        blacklistService.blacklist(jti);
    }

    public void blacklistByUser(String email) {
        blacklistService.blacklistByUser(email);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String jti = claims.getId();
            if (jti != null && blacklistService.isBlacklisted(jti)) {
                log.warn("Token is blacklisted: {}", jti.substring(0, Math.min(8, jti.length())));
                return false;
            }

            Date issuedAt = claims.getIssuedAt();
            Date now = new Date();
            if (issuedAt != null && issuedAt.after(now)) {
                log.error("Token issuedAt date is in the future");
                return false;
            }

            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(now)) {
                log.error("Token expiration time is in the past");
                return false;
            }

            if (claims.getSubject() == null || claims.getSubject().isBlank()) {
                log.error("Token subject is missing or empty");
                return false;
            }

            return true;
        } catch (ExpiredJwtException ex) {
            log.error("Expired token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token format not supported: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Malformed JWT Token: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.error("JWT token signing failed: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Illegal argument when validating JWT token: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error validating JWT token: {}", ex.getMessage());
        }
        return false;
    }
}
