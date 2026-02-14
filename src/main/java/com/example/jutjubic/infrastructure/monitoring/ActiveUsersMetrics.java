package com.example.jutjubic.infrastructure.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom metrika za pracenje broja aktivnih korisnika
 * Prometheus ce citati ove metrike putem /actuator/prometheus endpointa
 */
@Component
public class ActiveUsersMetrics implements MeterBinder {

    private final AtomicInteger activeUsersCount = new AtomicInteger(0);
    private final Map<String, Long> activeUsers = new ConcurrentHashMap<>();
    
    // Timeout za korisnika (30 minuta neaktivnosti)
    private static final long USER_TIMEOUT_MS = 30 * 60 * 1000;

    @Override
    public void bindTo(MeterRegistry registry) {
        // Registrujemo gauge metriku koja prati broj aktivnih korisnika
        registry.gauge("active_users_count", activeUsersCount);
    }

    /**
     * Registruje aktivnost korisnika
     * @param userEmail email korisnika
     */
    public void recordUserActivity(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        Long lastActivity = activeUsers.get(userEmail);
        
        if (lastActivity == null) {
            // Novi aktivni korisnik
            activeUsers.put(userEmail, currentTime);
            activeUsersCount.incrementAndGet();
        } else {
            // Azuriraj vreme poslednje aktivnosti
            activeUsers.put(userEmail, currentTime);
        }
        
        // Ukloni neaktivne korisnike
        cleanupInactiveUsers();
    }

    /**
     * Uklanja korisnike koji nisu bili aktivni duze od USER_TIMEOUT_MS
     */
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

    /**
     * Vraca trenutni broj aktivnih korisnika
     */
    public int getActiveUsersCount() {
        cleanupInactiveUsers();
        return activeUsersCount.get();
    }

    /**
     * Manuelno uklanja korisnika (npr. pri logout-u)
     */
    public void removeUser(String userEmail) {
        if (activeUsers.remove(userEmail) != null) {
            activeUsersCount.decrementAndGet();
        }
    }
}
