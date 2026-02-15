package com.example.jutjubic.infrastructure.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ActiveUsersMetrics implements MeterBinder {

    private final AtomicInteger activeUsersCount = new AtomicInteger(0);
    private final AtomicInteger anonymousUsersCount = new AtomicInteger(0);
    private final Map<String, Long> activeUsers = new ConcurrentHashMap<>();
    private final Map<String, Long> anonymousUsers = new ConcurrentHashMap<>();
    
    private static final long USER_TIMEOUT_MS = 30 * 60 * 1000;
    private static final long ANONYMOUS_TIMEOUT_MS = 5 * 60 * 1000;

    @Override
    public void bindTo(MeterRegistry registry) {
        registry.gauge("active_users_count", activeUsersCount);
        registry.gauge("anonymous_users_count", anonymousUsersCount);
    }

    public void recordUserActivity(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        Long lastActivity = activeUsers.get(userEmail);
        
        if (lastActivity == null) {
            activeUsers.put(userEmail, currentTime);
            activeUsersCount.incrementAndGet();
        } else {
            activeUsers.put(userEmail, currentTime);
        }
        
        cleanupInactiveUsers();
    }

    public void recordAnonymousActivity(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        Long lastActivity = anonymousUsers.get(sessionId);
        
        if (lastActivity == null) {
            anonymousUsers.put(sessionId, currentTime);
            anonymousUsersCount.incrementAndGet();
        } else {
            anonymousUsers.put(sessionId, currentTime);
        }
        
        cleanupAnonymousUsers();
    }

    private void cleanupInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        
        activeUsers.entrySet().removeIf(entry -> {
            boolean isInactive = (currentTime - entry.getValue()) > USER_TIMEOUT_MS;
            if (isInactive) {
                activeUsersCount.decrementAndGet();
            }
            return isInactive;
        });
    }

    private void cleanupAnonymousUsers() {
        long currentTime = System.currentTimeMillis();
        
        anonymousUsers.entrySet().removeIf(entry -> {
            boolean isInactive = (currentTime - entry.getValue()) > ANONYMOUS_TIMEOUT_MS;
            if (isInactive) {
                anonymousUsersCount.decrementAndGet();
            }
            return isInactive;
        });
    }

    public int getActiveUsersCount() {
        cleanupInactiveUsers();
        return activeUsersCount.get();
    }

    public int getAnonymousUsersCount() {
        cleanupAnonymousUsers();
        return anonymousUsersCount.get();
    }

    public int getTotalActiveUsers() {
        return getActiveUsersCount() + getAnonymousUsersCount();
    }

    public void removeUser(String userEmail) {
        if (activeUsers.remove(userEmail) != null) {
            activeUsersCount.decrementAndGet();
        }
    }
}
