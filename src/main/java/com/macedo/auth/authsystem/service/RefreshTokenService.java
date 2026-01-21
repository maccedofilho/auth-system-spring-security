package com.macedo.auth.authsystem.service;

import com.macedo.auth.authsystem.config.JwtProperties;
import com.macedo.auth.authsystem.dto.SessionResponse;
import com.macedo.auth.authsystem.entity.RefreshToken;
import com.macedo.auth.authsystem.entity.User;
import com.macedo.auth.authsystem.exception.TokenRefreshException;
import com.macedo.auth.authsystem.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final JwtProperties props;
    private final PasswordEncoder passwordEncoder;

    public RefreshTokenService(RefreshTokenRepository repo, JwtProperties props, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.props = props;
        this.passwordEncoder = passwordEncoder;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Transactional
    public String issue(User user) {
        repo.deleteByUser(user);
        var token = UUID.randomUUID().toString();
        var hashedToken = hashToken(token);
        var now = Instant.now();
        var rt = RefreshToken.builder()
                .user(user)
                .token(hashedToken)
                .expiryDate(now.plusMillis(props.getRefreshTokenExpirationMs()))
                .revoked(false)
                .createdAt(now)
                .lastUsedAt(now)
                .build();
        repo.save(rt);
        return token;
    }

    @Transactional
    public RefreshToken validateAndGetRefreshToken(String refreshToken) {
        var hashedToken = hashToken(refreshToken);
        var rt = repo.findByToken(hashedToken)
                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));
        if (rt.isRevoked()) {
            throw new TokenRefreshException("Refresh token revoked");
        }
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenRefreshException("Refresh token expired");
        }
        return rt;
    }

    @Transactional
    public String refresh(String oldRefreshToken) {
        var oldRt = validateAndGetRefreshToken(oldRefreshToken);

        oldRt.setRevoked(true);
        repo.save(oldRt);

        var newToken = UUID.randomUUID().toString();
        var hashedNewToken = hashToken(newToken);
        var now = Instant.now();
        var rt = RefreshToken.builder()
                .user(oldRt.getUser())
                .token(hashedNewToken)
                .expiryDate(now.plusMillis(props.getRefreshTokenExpirationMs()))
                .revoked(false)
                .deviceName(oldRt.getDeviceName())
                .ip(oldRt.getIp())
                .userAgent(oldRt.getUserAgent())
                .createdAt(now)
                .lastUsedAt(now)
                .build();
        repo.save(rt);
        return newToken;
    }

    @Transactional
    public void revokeAll(User user) {
        repo.deleteByUser(user);
    }

    @Transactional
    public void revoke(String refreshToken) {
        var rt = validateAndGetRefreshToken(refreshToken);
        rt.setRevoked(true);
        repo.save(rt);
    }

    public List<SessionResponse> getSessionsByUser(User user, String currentToken) {
        var tokens = repo.findByUserAndRevokedFalseOrderByCreatedAtDesc(user);
        String currentHashedToken = currentToken != null ? hashToken(currentToken) : null;

        return tokens.stream()
                .map(rt -> SessionResponse.builder()
                        .sessionId(rt.getId())
                        .deviceName(rt.getDeviceName())
                        .ip(rt.getIp())
                        .userAgent(rt.getUserAgent())
                        .createdAt(rt.getCreatedAt())
                        .lastUsedAt(rt.getLastUsedAt())
                        .isCurrent(currentHashedToken != null && currentHashedToken.equals(rt.getToken()))
                        .expiresAt(rt.getExpiryDate())
                        .build())
                .toList();
    }
}
