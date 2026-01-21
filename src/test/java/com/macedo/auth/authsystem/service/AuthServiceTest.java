package com.macedo.auth.authsystem.service;

import com.macedo.auth.authsystem.config.JwtProperties;
import com.macedo.auth.authsystem.dto.ChangePasswordRequest;
import com.macedo.auth.authsystem.entity.Role;
import com.macedo.auth.authsystem.entity.RoleName;
import com.macedo.auth.authsystem.entity.User;
import com.macedo.auth.authsystem.exception.InvalidCredentialsException;
import com.macedo.auth.authsystem.repository.RoleRepository;
import com.macedo.auth.authsystem.repository.UserRepository;
import com.macedo.auth.authsystem.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private RoleRepository roles;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtTokenProvider jwt;

    @Mock
    private JwtProperties props;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(props.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(props.getRefreshTokenExpirationMs()).thenReturn(86400000L);
        authService = new AuthService(users, roles, encoder, jwt, props, refreshTokenService);
    }

    @Test
    void whenChangePassword_withValidCurrentPassword_thenChangesPasswordAndRevokesSessions() {
        String email = "user@example.com";
        String currentPassword = "OldPass123";
        String newPassword = "NewPass456";
        User user = User.builder()
                .id(1L)
                .email(email)
                .password("hashed-old-password")
                .roles(Set.of(Role.builder().name(RoleName.ROLE_USER).build()))
                .enabled(true)
                .build();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);

        when(users.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.matches(currentPassword, user.getPassword())).thenReturn(true);
        when(encoder.matches(newPassword, user.getPassword())).thenReturn(false);
        when(encoder.encode(newPassword)).thenReturn("hashed-new-password");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.changePassword(email, request);

        assertEquals("hashed-new-password", user.getPassword());
        verify(refreshTokenService).revokeAll(user);
        verify(users).save(user);
    }

    @Test
    void whenChangePassword_withIncorrectCurrentPassword_thenThrowsException() {
        String email = "user@example.com";
        String wrongPassword = "WrongPass123";
        String newPassword = "NewPass456";
        User user = User.builder()
                .id(1L)
                .email(email)
                .password("hashed-password")
                .roles(Set.of(Role.builder().name(RoleName.ROLE_USER).build()))
                .enabled(true)
                .build();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(wrongPassword);
        request.setNewPassword(newPassword);

        when(users.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.matches(wrongPassword, user.getPassword())).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> {
            authService.changePassword(email, request);
        });

        assertTrue(ex.getMessage().contains("Current password is incorrect"));
        verify(refreshTokenService, never()).revokeAll(any());
        verify(users, never()).save(any());
    }

    @Test
    void whenChangePassword_withSameAsCurrentPassword_thenThrowsException() {
        String email = "user@example.com";
        String currentPassword = "SamePass123";
        User user = User.builder()
                .id(1L)
                .email(email)
                .password("hashed-password")
                .roles(Set.of(Role.builder().name(RoleName.ROLE_USER).build()))
                .enabled(true)
                .build();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(currentPassword);

        when(users.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.matches(currentPassword, user.getPassword())).thenReturn(true);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> {
            authService.changePassword(email, request);
        });

        assertTrue(ex.getMessage().contains("New password must be different from current password"));
        verify(refreshTokenService, never()).revokeAll(any());
        verify(users, never()).save(any());
    }

    @Test
    void whenChangePassword_withNonExistentUser_thenThrowsException() {
        String email = "nonexistent@example.com";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPass123");
        request.setNewPassword("NewPass456");

        when(users.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.changePassword(email, request);
        });

        verify(refreshTokenService, never()).revokeAll(any());
        verify(users, never()).save(any());
    }

    @Test
    void whenChangePassword_withMinimumValidPassword_thenChangesPassword() {
        String email = "user@example.com";
        String currentPassword = "OldPass123";
        String newPassword = "12345678"; // 8 characters - minimum
        User user = User.builder()
                .id(1L)
                .email(email)
                .password("hashed-old-password")
                .roles(Set.of(Role.builder().name(RoleName.ROLE_USER).build()))
                .enabled(true)
                .build();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);

        when(users.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.matches(currentPassword, user.getPassword())).thenReturn(true);
        when(encoder.matches(newPassword, user.getPassword())).thenReturn(false);
        when(encoder.encode(newPassword)).thenReturn("hashed-new-password");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> authService.changePassword(email, request));
        assertEquals("hashed-new-password", user.getPassword());
        verify(refreshTokenService).revokeAll(user);
    }
}
