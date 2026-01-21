package com.macedo.auth.authsystem.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.macedo.auth.authsystem.dto.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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

    private record RateLimitConfig(String endpoint, int capacity, int durationMinutes) implements Serializable {
    }

    private static final RateLimitConfig LOGIN_LIMIT = new RateLimitConfig("/api/auth/login", 5, 1);
    private static final RateLimitConfig REGISTER_LIMIT = new RateLimitConfig("/api/auth/register", 3, 1);
    private static final RateLimitConfig REFRESH_LIMIT = new RateLimitConfig("/api/auth/refresh", 10, 1);
    private static final RateLimitConfig CHANGE_PASSWORD_LIMIT = new RateLimitConfig("/api/auth/change-password", 3, 1);

    private Bucket createNewBucket(RateLimitConfig config) {
        Refill refill = Refill.intervally(config.capacity, Duration.ofMinutes(config.durationMinutes));
        Bandwidth limit = Bandwidth.classic(config.capacity, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket getBucket(String key, RateLimitConfig config) {
        return buckets.get(key, k -> createNewBucket(config));
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

    private void writeRateLimitResponse(HttpServletResponse response, String path, long retryAfterSeconds) throws IOException {
        ErrorResponse error = ErrorResponse.builder()
                .code("RATE_LIMIT_EXCEEDED")
                .message("Too many requests. Please try again in " + retryAfterSeconds + " seconds.")
                .timestamp(Instant.now())
                .path(path)
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.getWriter().write(objectMapper.writeValueAsString(error));

        log.warn("Rate limit exceeded for path: {}, retry after: {}s", path, retryAfterSeconds);
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

        // verifica rate limit para login / check rate limit for login
        if (path.startsWith(LOGIN_LIMIT.endpoint())) {
            rateLimited = checkRateLimit(request, response, path, LOGIN_LIMIT);
        }

        // verifica rate limit para register / check rate limit for register
        if (!rateLimited && path.startsWith(REGISTER_LIMIT.endpoint())) {
            rateLimited = checkRateLimit(request, response, path, REGISTER_LIMIT);
        }

        // verifica rate limit para refresh / check rate limit for refresh
        if (!rateLimited && path.startsWith(REFRESH_LIMIT.endpoint())) {
            rateLimited = checkRateLimit(request, response, path, REFRESH_LIMIT);
        }

        // verifica rate limit para change-password / check rate limit for change-password
        if (!rateLimited && path.startsWith(CHANGE_PASSWORD_LIMIT.endpoint())) {
            rateLimited = checkRateLimit(request, response, path, CHANGE_PASSWORD_LIMIT);
        }

        if (!rateLimited) {
            filterChain.doFilter(request, response);
        }
    }

    private boolean checkRateLimit(HttpServletRequest request, HttpServletResponse response,
                                   String path, RateLimitConfig config) throws IOException {
        String clientIdentifier = getClientIdentifier(request);
        String bucketKey = config.endpoint().substring(config.endpoint().lastIndexOf('/')) + ":" + clientIdentifier;
        Bucket bucket = getBucket(bucketKey, config);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(config.capacity()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            return false;

        } else {
            long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setHeader("X-RateLimit-Limit", String.valueOf(config.capacity()));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(retryAfterSeconds));

            writeRateLimitResponse(response, path, retryAfterSeconds);
            log.warn("Rate limit exceeded for path: {} by client: {}", path, clientIdentifier);
            return true;
        }
    }
}
