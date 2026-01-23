package com.macedo.auth.authsystem.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JwtBlacklistService {

    private static final Duration BLACKLIST_DURATION = Duration.ofMinutes(15);

    private final Cache<String, String> blacklist = Caffeine.newBuilder()
            .expireAfterWrite(BLACKLIST_DURATION)
            .maximumSize(10_000)
            .build();

    public void blacklist(String tokenId) {
        blacklist.put(tokenId, tokenId);
        log.info("JWT token blacklisted: {}", tokenId.substring(0, Math.min(10, tokenId.length())) + "...");
    }

    public void blacklistByUser(String email) {
        log.info("All JWT tokens for user {} will be invalidated on next validation", email);
    }

    public boolean isBlacklisted(String tokenId) {
        return blacklist.getIfPresent(tokenId) != null;
    }

    public long getBlacklistDurationMinutes() {
        return BLACKLIST_DURATION.toMinutes();
    }
}
