package com.example.jutjubic.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // Cache za thumbnails - 60 minuta
        CaffeineCache thumbnailsCache = new CaffeineCache("thumbnails",
                Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .build());

        // Cache za loginAttempts (rate limiter) - 1 minut
        CaffeineCache loginAttemptsCache = new CaffeineCache("loginAttempts",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .build());

        // Cache za comments - 10 minuta
        CaffeineCache commentsCache = new CaffeineCache("comments",
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(1000)  // Maksimalno 1000 kesiranih rezultata
                        .build());

        cacheManager.setCaches(Arrays.asList(
                thumbnailsCache,
                loginAttemptsCache,
                commentsCache
        ));

        return cacheManager;
    }
}