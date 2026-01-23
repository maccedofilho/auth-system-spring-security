package com.macedo.auth.authsystem.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final Cache<String, Integer> attemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(LOCKOUT_DURATION)
            .maximumSize(10_000)
            .build();

    private final Cache<String, Long> lockedAccountsCache = Caffeine.newBuilder()
            .expireAfterWrite(LOCKOUT_DURATION)
            .maximumSize(10_000)
            .build();

    public void loginFailed(String identifier) {
        int attempts = attemptsCache.get(identifier, k -> 0) + 1;
        attemptsCache.put(identifier, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            lockAccount(identifier);
            log.warn("Account locked after {} failed attempts: {}", attempts, identifier);
        } else {
            log.warn("Login failed for {} (attempt {}/{})", identifier, attempts, MAX_ATTEMPTS);
        }
    }

    public void loginSucceeded(String identifier) {
        attemptsCache.invalidate(identifier);
        lockedAccountsCache.invalidate(identifier);
    }

    public boolean isLocked(String identifier) {
        Long lockedUntil = lockedAccountsCache.getIfPresent(identifier);
        if (lockedUntil != null) {
            long remainingMinutes = (lockedUntil - System.currentTimeMillis()) / 60_000;
            if (remainingMinutes > 0) {
                log.warn("Login attempt for locked account: {} ({} minutes remaining)", identifier, remainingMinutes);
                return true;
            } else {
                lockedAccountsCache.invalidate(identifier);
                attemptsCache.invalidate(identifier);
            }
        }
        return false;
    }

    private void lockAccount(String identifier) {
        lockedAccountsCache.put(identifier, System.currentTimeMillis() + LOCKOUT_DURATION.toMillis());
        attemptsCache.invalidate(identifier);
    }

    public int getRemainingAttempts(String identifier) {
        if (isLocked(identifier)) {
            return 0;
        }
        int attempts = attemptsCache.get(identifier, k -> 0);
        return MAX_ATTEMPTS - attempts;
    }

    public long getLockoutTimeRemaining(String identifier) {
        Long lockedUntil = lockedAccountsCache.getIfPresent(identifier);
        if (lockedUntil == null) {
            return 0;
        }
        long remaining = lockedUntil - System.currentTimeMillis();
        return Math.max(0, remaining / 60_000);
    }
}
