package com.macedo.auth.authsystem.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.macedo.auth.authsystem.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void whenEmailAlreadyExistsException_thenReturns409Conflict() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email already in use");
        when(request.getRequestURI()).thenReturn("/api/auth/register");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEmailAlreadyExists(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("EMAIL_ALREADY_EXISTS", response.getBody().getCode());
        assertEquals("Email already registered", response.getBody().getMessage());
        assertEquals("/api/auth/register", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void whenInvalidCredentialsException_thenReturns401Unauthorized() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", response.getBody().getCode());
        assertEquals("Invalid email or password", response.getBody().getMessage());
        assertEquals("/api/auth/login", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void whenResourceNotFoundException_thenReturns404NotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        when(request.getRequestURI()).thenReturn("/api/users/999");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getCode());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals("/api/users/999", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void whenGenericException_thenReturns500InternalServerError() {
        Exception ex = new Exception("Internal error");
        when(request.getRequestURI()).thenReturn("/api/some-endpoint");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("/api/some-endpoint", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void whenErrorResponseSerialized_thenContainsRequiredFields() throws JsonProcessingException {
        ErrorResponse error = ErrorResponse.builder()
                .code("TEST_CODE")
                .message("Test message")
                .timestamp(java.time.Instant.now())
                .path("/test/path")
                .build();

        String json = objectMapper.writeValueAsString(error);

        assertTrue(json.contains("\"code\":\"TEST_CODE\""));
        assertTrue(json.contains("\"message\":\"Test message\""));
        assertTrue(json.contains("\"path\":\"/test/path\""));
        assertTrue(json.contains("\"timestamp\""));
    }
}
