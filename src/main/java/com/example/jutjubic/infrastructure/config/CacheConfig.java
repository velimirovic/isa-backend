package com.example.jutjubic.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // Cache za thumbnails - 60 minuta
        CaffeineCache thumbnailsCache = new CaffeineCache("thumbnails",
                Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .build());

        // Cache za comments - 10 minuta
        CaffeineCache commentsCache = new CaffeineCache("comments",
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(1000)  // Maksimalno 1000 kesiranih rezultata
                        .build());

        // Cache za map tiles - 30 minuta
        CaffeineCache mapTilesCache = new CaffeineCache("mapTiles",
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .maximumSize(500)  // Maksimalno 500 tile kombinacija
                        .build());

        cacheManager.setCaches(Arrays.asList(
                thumbnailsCache,
                commentsCache,
                mapTilesCache
        ));

        return cacheManager;
    }
}