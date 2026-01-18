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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Slf4j
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

    private String getClientIdentifier(HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null || userAgent.isEmpty() || userAgent.length() > 255) {
            userAgent = "unknown";
        }

        return ip + ":" + userAgent.hashCode();
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
        response.setHeader("X-RateLimit-Limit", "5");
        response.setHeader("Retry-After", "60");
        response.getWriter().write(objectMapper.writeValueAsString(error));

        log.warn("Rate limit exceeded for path: {}", path);
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

        boolean rateLimited = false;

        if (path.startsWith("/api/auth/login")) {
            String clientIdentifier = getClientIdentifier(request);
            Bucket bucket = getBucket("login:" + clientIdentifier, 5, 1);

            if (!bucket.tryConsume(1)) {
                writeRateLimitResponse(response, path);
                rateLimited = true;
                log.warn("Login rate limit exceeded for client: {}", clientIdentifier);
            }
        }

        if (!rateLimited && path.startsWith("/api/auth/register")) {
            String clientIdentifier = getClientIdentifier(request);
            Bucket bucket = getBucket("register:" + clientIdentifier, 3, 1);

            if (!bucket.tryConsume(1)) {
                writeRateLimitResponse(response, path);
                rateLimited = true;
                log.warn("Registration rate limit exceeded for client: {}", clientIdentifier);
            }
        }

        if (!rateLimited && path.startsWith("/api/auth/refresh")) {
            String clientIdentifier = getClientIdentifier(request);
            Bucket bucket = getBucket("refresh:" + clientIdentifier, 10, 1);

            if (!bucket.tryConsume(1)) {
                writeRateLimitResponse(response, path);
                rateLimited = true;
                log.warn("Refresh token rate limit exceeded for client: {}", clientIdentifier);
            }
        }

        if (!rateLimited) {
            filterChain.doFilter(request, response);
        }
    }
}
