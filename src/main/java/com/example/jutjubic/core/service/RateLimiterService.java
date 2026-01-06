package com.example.jutjubic.core.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;

@Service
public class RateLimiterService {

    @Autowired
    private CacheManager cacheManager;

    private static final int MAX_ATTEMPTS = 5;
    private static final int TIME_WINDOW_MINUTES = 1;
    private static final String CACHE_NAME = "loginAttempts";

    public boolean isAllowed(String ipAddress) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) return true;

        LoginAttemptCache attempt = cache.get(ipAddress, LoginAttemptCache.class);

        if (attempt == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = attempt.getFirstAttempt().plusMinutes(TIME_WINDOW_MINUTES);

        if (now.isAfter(expiryTime)) {
            cache.evict(ipAddress);
            return true;
        }

        return attempt.getCount() < MAX_ATTEMPTS;
    }

    public void registerAttempt(String ipAddress) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) return;

        LoginAttemptCache attempt = cache.get(ipAddress, LoginAttemptCache.class);

        if (attempt == null) {
            cache.put(ipAddress, new LoginAttemptCache(1, LocalDateTime.now()));
        } else {
            attempt.setCount(attempt.getCount() + 1);
            cache.put(ipAddress, attempt);
        }
    }

    public void resetAttempts(String ipAddress) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(ipAddress);
        }
    }

    public int getRemainingAttempts(String ipAddress) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) return MAX_ATTEMPTS;

        LoginAttemptCache attempt = cache.get(ipAddress, LoginAttemptCache.class);

        if (attempt == null) {
            return MAX_ATTEMPTS;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = attempt.getFirstAttempt().plusMinutes(TIME_WINDOW_MINUTES);

        if (now.isAfter(expiryTime)) {
            return MAX_ATTEMPTS;
        }

        return Math.max(0, MAX_ATTEMPTS - attempt.getCount());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginAttemptCache implements Serializable {
        private static final long serialVersionUID = 1L;
        private int count;
        private LocalDateTime firstAttempt;
    }
}