package com.macedo.auth.authsystem.service;

import com.macedo.auth.authsystem.dto.ResetPasswordRequest;
import com.macedo.auth.authsystem.entity.PasswordResetToken;
import com.macedo.auth.authsystem.entity.Role;
import com.macedo.auth.authsystem.entity.User;
import com.macedo.auth.authsystem.exception.InvalidResetTokenException;
import com.macedo.auth.authsystem.repository.PasswordResetTokenRepository;
import com.macedo.auth.authsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                tokenRepository,
                userRepository,
                passwordEncoder,
                emailService,
                refreshTokenService
        );
    }

    @Test
    void whenInitiatePasswordReset_withExistingEmail_thenCreatesTokenAndSendsEmail() {
        String email = "user@example.com";
        String ipAddress = "192.168.1.1";

        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(tokenRepository.findByEmailAndUsedFalseAndExpiryDateAfter(anyString(), any(Instant.class)))
                .thenReturn(java.util.List.of());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> {
            PasswordResetToken token = invocation.getArgument(0);
            token.setId(1L);
            return token;
        });

        assertDoesNotThrow(() -> passwordResetService.initiatePasswordReset(email, ipAddress));

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq(email), anyString(), isNull());
    }

    @Test
    void whenInitiatePasswordReset_withNonExistentEmail_thenDoesNotRevealUserNonExistence() {
        String email = "nonexistent@example.com";
        String ipAddress = "192.168.1.1";

        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertDoesNotThrow(() -> passwordResetService.initiatePasswordReset(email, ipAddress));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void whenResetPassword_withValidToken_thenResetsPasswordAndRevokesSessions() {
        String token = "valid-token";
        String email = "user@example.com";
        String newPassword = "NewPassword123";

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(1L)
                .email(email)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(3600))
                .used(false)
                .createdAt(Instant.now())
                .build();

        User user = User.builder()
                .id(1L)
                .email(email)
                .password("old-hashed-password")
                .roles(Set.of(Role.builder().name(com.macedo.auth.authsystem.entity.RoleName.ROLE_USER).build()))
                .enabled(true)
                .build();

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword(newPassword);

        when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(resetToken));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("new-hashed-password");
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.resetPassword(request);

        assertEquals("new-hashed-password", user.getPassword());
        assertTrue(resetToken.isUsed());
        assertNotNull(resetToken.getUsedAt());
        verify(refreshTokenService).revokeAll(user);
    }

    @Test
    void whenResetPassword_withExpiredToken_thenThrowsException() {
        String token = "expired-token";

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(1L)
                .email("user@example.com")
                .token(token)
                .expiryDate(Instant.now().minusSeconds(3600))
                .used(false)
                .createdAt(Instant.now().minusSeconds(7200))
                .build();

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword("NewPassword123");

        when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(resetToken));

        InvalidResetTokenException ex = assertThrows(InvalidResetTokenException.class, () -> {
            passwordResetService.resetPassword(request);
        });

        assertTrue(ex.getMessage().contains("expired"));
        verify(refreshTokenService, never()).revokeAll(any());
    }

    @Test
    void whenResetPassword_withUsedToken_thenThrowsException() {
        String token = "used-token";

        when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword("NewPassword123");

        InvalidResetTokenException ex = assertThrows(InvalidResetTokenException.class, () -> {
            passwordResetService.resetPassword(request);
        });

        assertTrue(ex.getMessage().contains("Invalid") || ex.getMessage().contains("expired"));
        verify(refreshTokenService, never()).revokeAll(any());
    }

    @Test
    void whenResetPassword_withEmptyToken_thenThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("");
        request.setNewPassword("NewPassword123");

        InvalidResetTokenException ex = assertThrows(InvalidResetTokenException.class, () -> {
            passwordResetService.resetPassword(request);
        });

        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    void whenCleanupExpiredTokens_thenDeletesExpiredTokens() {
        passwordResetService.cleanupExpiredTokens();

        verify(tokenRepository).deleteByExpiryDateBefore(any(Instant.class));
    }
}
