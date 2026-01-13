package com.macedo.auth.authsystem.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtPropertiesTest {

    @Test
    void validate_WhenSecretIsNull_ShouldThrowIllegalStateException() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, jwtProperties::validate);

        assertEquals("JWT_SECRET is missing or too weak (min 32 chars).", exception.getMessage());
    }

    @Test
    void validate_WhenSecretIsBlank_ShouldThrowIllegalStateException() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("   ");

        IllegalStateException exception = assertThrows(IllegalStateException.class, jwtProperties::validate);

        assertEquals("JWT_SECRET is missing or too weak (min 32 chars).", exception.getMessage());
    }

    @Test
    void validate_WhenSecretIsLessThan32Chars_ShouldThrowIllegalStateException() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("shortsecret");

        IllegalStateException exception = assertThrows(IllegalStateException.class, jwtProperties::validate);

        assertEquals("JWT_SECRET is missing or too weak (min 32 chars).", exception.getMessage());
    }

    @Test
    void validate_WhenSecretIsExactly32Chars_ShouldNotThrowException() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("12345678901234567890123456789012");

        assertDoesNotThrow(jwtProperties::validate);
    }

    @Test
    void validate_WhenSecretIsMoreThan32Chars_ShouldNotThrowException() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("1234567890123456789012345678901234567890");

        assertDoesNotThrow(jwtProperties::validate);
    }
}
