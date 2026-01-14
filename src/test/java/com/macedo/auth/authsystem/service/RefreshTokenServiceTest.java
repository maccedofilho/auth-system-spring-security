package com.macedo.auth.authsystem.service;

import com.macedo.auth.authsystem.config.JwtProperties;
import com.macedo.auth.authsystem.entity.RefreshToken;
import com.macedo.auth.authsystem.entity.User;
import com.macedo.auth.authsystem.exception.TokenRefreshException;
import com.macedo.auth.authsystem.repository.RefreshTokenRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repo;

    @Mock
    private JwtProperties props;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        when(props.getRefreshTokenExpirationMs()).thenReturn(86400000L);
        refreshTokenService = new RefreshTokenService(repo, props, passwordEncoder);
    }

    @Test
    void whenIssueToken_thenReturnsTokenAndSavesHashed() {
        User user = User.builder().id(1L).email("test@example.com").build();

        doNothing().when(repo).deleteByUser(any(User.class));
        when(repo.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = refreshTokenService.issue(user);

        assertNotNull(token);
        verify(repo).deleteByUser(user);
        verify(repo).save(any(RefreshToken.class));
    }

    @Test
    void whenValidateValidToken_thenReturnsRefreshToken() {
        String token = "valid-token";
        RefreshToken rt = RefreshToken.builder()
                .id(1L)
                .token("hashed-token")
                .expiryDate(Instant.now().plusMillis(86400000))
                .revoked(false)
                .build();

        when(repo.findByToken(any())).thenReturn(Optional.of(rt));

        RefreshToken result = refreshTokenService.validateAndGetRefreshToken(token);

        assertNotNull(result);
        assertEquals(rt.getId(), result.getId());
    }

    @Test
    void whenValidateNotFoundToken_thenThrowsException() {
        String token = "non-existent-token";

        when(repo.findByToken(any())).thenReturn(Optional.empty());

        assertThrows(TokenRefreshException.class, () -> {
            refreshTokenService.validateAndGetRefreshToken(token);
        });
    }

    @Test
    void whenValidateRevokedToken_thenThrowsException() {
        String token = "revoked-token";
        RefreshToken rt = RefreshToken.builder()
                .id(1L)
                .token("hashed-token")
                .expiryDate(Instant.now().plusMillis(86400000))
                .revoked(true)
                .build();

        when(repo.findByToken(any())).thenReturn(Optional.of(rt));

        TokenRefreshException ex = assertThrows(TokenRefreshException.class, () -> {
            refreshTokenService.validateAndGetRefreshToken(token);
        });

        assertTrue(ex.getMessage().contains("revoked"));
    }

    @Test
    void whenValidateExpiredToken_thenThrowsException() {
        String token = "expired-token";
        RefreshToken rt = RefreshToken.builder()
                .id(1L)
                .token("hashed-token")
                .expiryDate(Instant.now().minusMillis(1000))
                .revoked(false)
                .build();

        when(repo.findByToken(any())).thenReturn(Optional.of(rt));

        TokenRefreshException ex = assertThrows(TokenRefreshException.class, () -> {
            refreshTokenService.validateAndGetRefreshToken(token);
        });

        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void whenRefreshToken_thenRevokesOldAndIssuesNew() {
        String oldToken = "old-token";
        User user = User.builder().id(1L).email("test@example.com").build();

        RefreshToken oldRt = RefreshToken.builder()
                .id(1L)
                .token("hashed-old-token")
                .user(user)
                .expiryDate(Instant.now().plusMillis(86400000))
                .revoked(false)
                .build();

        when(repo.findByToken(any())).thenReturn(Optional.of(oldRt));
        when(repo.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String newToken = refreshTokenService.refresh(oldToken);

        assertNotNull(newToken);
        assertNotEquals(oldToken, newToken);
        assertTrue(oldRt.isRevoked());
        verify(repo, times(2)).save(any(RefreshToken.class));
    }
}
