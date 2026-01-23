package com.macedo.auth.authsystem.service;

import com.macedo.auth.authsystem.dto.ResetPasswordRequest;
import com.macedo.auth.authsystem.entity.PasswordResetToken;
import com.macedo.auth.authsystem.entity.User;
import com.macedo.auth.authsystem.exception.InvalidResetTokenException;
import com.macedo.auth.authsystem.repository.PasswordResetTokenRepository;
import com.macedo.auth.authsystem.repository.UserRepository;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PasswordResetService {

    private static final int TOKEN_EXPIRATION_HOURS = 1;
    private static final int MAX_ACTIVE_TOKENS_PER_EMAIL = 3;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            RefreshTokenService refreshTokenService
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void initiatePasswordReset(String email, String ipAddress) {
        // Sempre retorna sucesso para não leakar existência de conta
        boolean userExists = userRepository.existsByEmail(email);

        if (!userExists) {
            log.info("Password reset requested for non-existent email: {} from IP: {}", email, ipAddress);
            return;
        }

        // Limpa tokens anteriores e cria novo
        invalidatePreviousTokens(email);
        String token = generateResetToken(email, ipAddress);

        log.info("Password reset token generated for email: {} from IP: {}", email, ipAddress);
        emailService.sendPasswordResetEmail(email, token, frontendUrl);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = validateToken(request.getToken());

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new InvalidResetTokenException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetToken.setUsedAt(Instant.now());
        tokenRepository.save(resetToken);

        refreshTokenService.revokeAll(user);

        log.info("Password reset completed for user: {} from token issued at {}",
                user.getEmail(), resetToken.getCreatedAt());
    }

    private PasswordResetToken validateToken(@Nonnull String rawToken) {
        if (rawToken.isBlank()) {
            throw new InvalidResetTokenException("Token cannot be empty");
        }

        PasswordResetToken token = tokenRepository.findByTokenAndUsedFalse(rawToken)
                .orElseThrow(() -> new InvalidResetTokenException("Invalid or expired token"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidResetTokenException("Token has expired");
        }

        return token;
    }

    private String generateResetToken(String email, String ipAddress) {
        Instant expiryDate = Instant.now().plusSeconds(TOKEN_EXPIRATION_HOURS * 3600L);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .token(UUID.randomUUID().toString())
                .expiryDate(expiryDate)
                .ipAddress(ipAddress)
                .build();

        return tokenRepository.save(resetToken).getToken();
    }

    private void invalidatePreviousTokens(String email) {
        List<PasswordResetToken> previousTokens = tokenRepository
                .findByEmailAndUsedFalseAndExpiryDateAfter(email, Instant.now());

        if (previousTokens.size() >= MAX_ACTIVE_TOKENS_PER_EMAIL) {
            previousTokens.stream()
                    .limit(previousTokens.size() - MAX_ACTIVE_TOKENS_PER_EMAIL + 1)
                    .forEach(tokenRepository::delete);
        }
    }

    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(Instant.now());
        log.info("Cleaned up expired password reset tokens");
    }
}
