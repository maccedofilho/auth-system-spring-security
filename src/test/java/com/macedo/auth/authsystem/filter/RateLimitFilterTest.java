package com.macedo.auth.authsystem.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
    }

    @Test
    void whenOptionsRequest_thenDoesNotRateLimit() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("OPTIONS");
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void whenLoginRequestUnderLimit_thenSucceeds() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(5)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void whenLoginRequestExceedsLimit_thenReturns429() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(response.getWriter()).thenReturn(writer);

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
    }

    @Test
    void whenRegisterRequestUnderLimit_thenSucceeds() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/auth/register");
        when(request.getRemoteAddr()).thenReturn("127.0.0.2");

        for (int i = 0; i < 3; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(3)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void whenRegisterRequestExceedsLimit_thenReturns429() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/auth/register");
        when(request.getRemoteAddr()).thenReturn("192.168.1.2");
        when(response.getWriter()).thenReturn(writer);

        for (int i = 0; i < 3; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
    }

    @Test
    void whenPathWithTrailingSlash_thenStillMatches() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/auth/login/");

        assertDoesNotThrow(() -> rateLimitFilter.doFilterInternal(request, response, filterChain));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void whenVersionedPath_thenStillMatches() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        assertDoesNotThrow(() -> rateLimitFilter.doFilterInternal(request, response, filterChain));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void whenUnmatchedPath_thenDoesNotRateLimit() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/other/endpoint");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
}
