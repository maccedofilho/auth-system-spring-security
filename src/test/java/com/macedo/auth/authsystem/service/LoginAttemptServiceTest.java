package com.macedo.auth.authsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    void whenNewUser_thenHasMaxAttempts() {
        assertEquals(5, loginAttemptService.getRemainingAttempts("test@example.com"));
    }

    @Test
    void whenLoginFailed_thenDecreasesRemainingAttempts() {
        loginAttemptService.loginFailed("test@example.com");
        assertEquals(4, loginAttemptService.getRemainingAttempts("test@example.com"));
    }

    @Test
    void whenMultipleFailedLoginAttempts_thenDecreasesRemainingAttempts() {
        for (int i = 0; i < 3; i++) {
            loginAttemptService.loginFailed("test@example.com");
        }
        assertEquals(2, loginAttemptService.getRemainingAttempts("test@example.com"));
    }

    @Test
    void whenMaxAttemptsReached_thenAccountIsLocked() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed("test@example.com");
        }
        assertTrue(loginAttemptService.isLocked("test@example.com"));
        assertEquals(0, loginAttemptService.getRemainingAttempts("test@example.com"));
    }

    @Test
    void whenAccountLocked_thenReturnsPositiveLockoutTime() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed("test@example.com");
        }
        long remaining = loginAttemptService.getLockoutTimeRemaining("test@example.com");
        assertTrue(remaining > 0);
        assertTrue(remaining <= 15);
    }

    @Test
    void whenLoginSucceeded_thenResetsAttempts() {
        loginAttemptService.loginFailed("test@example.com");
        loginAttemptService.loginFailed("test@example.com");
        assertEquals(3, loginAttemptService.getRemainingAttempts("test@example.com"));

        loginAttemptService.loginSucceeded("test@example.com");
        assertEquals(5, loginAttemptService.getRemainingAttempts("test@example.com"));
        assertFalse(loginAttemptService.isLocked("test@example.com"));
    }

    @Test
    void whenLoginSucceededOnLockedAccount_thenUnlocks() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed("test@example.com");
        }
        assertTrue(loginAttemptService.isLocked("test@example.com"));

        loginAttemptService.loginSucceeded("test@example.com");
        assertFalse(loginAttemptService.isLocked("test@example.com"));
        assertEquals(5, loginAttemptService.getRemainingAttempts("test@example.com"));
    }

    @Test
    void whenDifferentIdentifiers_thenHaveIndependentAttempts() {
        loginAttemptService.loginFailed("user1@example.com");
        loginAttemptService.loginFailed("user1@example.com");
        loginAttemptService.loginFailed("user2@example.com");

        assertEquals(3, loginAttemptService.getRemainingAttempts("user1@example.com"));
        assertEquals(4, loginAttemptService.getRemainingAttempts("user2@example.com"));
    }
}
