package com.macedo.auth.authsystem.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.macedo.auth.authsystem.dto.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private final ObjectMapper objectMapper;

    public RateLimitFilter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private Bucket createNewBucket(int capacity, int durationMinutes) {
        Refill refill = Refill.intervally(capacity, Duration.ofMinutes(durationMinutes));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket getBucket(String key, int capacity, int durationMinutes) {
        return buckets.get(key, k -> createNewBucket(capacity, durationMinutes));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private void writeRateLimitResponse(HttpServletResponse response, String path) throws IOException {
        ErrorResponse error = ErrorResponse.builder()
                .code("RATE_LIMIT_EXCEEDED")
                .message("Too many requests, try again later")
                .timestamp(Instant.now())
                .path(path)
                .build();

        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/auth/login")) {
            String ip = getClientIp(request);
            Bucket bucket = getBucket("login:" + ip, 5, 1);

            if (!bucket.tryConsume(1)) {
                writeRateLimitResponse(response, path);
                return;
            }
        }

        if (path.startsWith("/api/auth/register")) {
            String ip = getClientIp(request);
            Bucket bucket = getBucket("register:" + ip, 3, 1);

            if (!bucket.tryConsume(1)) {
                writeRateLimitResponse(response, path);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
