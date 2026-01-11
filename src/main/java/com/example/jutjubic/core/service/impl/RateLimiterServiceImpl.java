package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.core.service.RateLimiterService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//Ogranicavanje pokusaja logovanja
@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    //Broj pokusaja i vreme prvog
    private static class LoginAttempt {
        int count;
        LocalDateTime firstAttempt;

        public LoginAttempt() {
            this.count = 1;
            this.firstAttempt = LocalDateTime.now();
        }
    }

    // Mapa koja cuva pokusaje po IP adresi
    // Key = IP adresa, Value = LoginAttempt objekat
    private final Map<String, LoginAttempt> attemptCache = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5;  // Maksimalno pokusaja
    private static final int TIME_WINDOW_MINUTES = 1;  // U roku od 1 minuta

    // Proverava ip adresu
    public boolean isAllowed(String ipAddress) {
        // Ocisti stare pokusaje
        cleanupExpiredAttempts();

        // Proveri da li postoje pokusaji za ovu IP adresu
        LoginAttempt attempt = attemptCache.get(ipAddress);

        if (attempt == null) {
            return true;
        }

        // Proveri da li je prosao 1 minut
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = attempt.firstAttempt.plusMinutes(TIME_WINDOW_MINUTES);

        // Ako je isteklo vreme zaboravi ip adresu
        if (now.isAfter(expiryTime)) {
            attemptCache.remove(ipAddress);
            return true;
        }

        // Ako je prekoracio limit
        if (attempt.count >= MAX_ATTEMPTS) {
            return false;
        }

        return true;
    }

    // Registruj pokusaj logovanja
    public void registerAttempt(String ipAddress) {
        attemptCache.compute(ipAddress, (key, attempt) -> {
            if (attempt == null) {
                // Prvi pokusaj za ovaj ip
                return new LoginAttempt();
            }
            else {
                // Brojac
                attempt.count++;
                return attempt;
            }
        });
    }

    // Kad se uspesno loguje brise pokusaje
    public void resetAttempts(String ipAddress) {
        attemptCache.remove(ipAddress);
    }

    // Obrisi pokusaje ako je prosao minut
    private void cleanupExpiredAttempts() {
        LocalDateTime now = LocalDateTime.now();
        attemptCache.entrySet().removeIf(entry -> {
            LocalDateTime expiryTime = entry.getValue().firstAttempt.plusMinutes(TIME_WINDOW_MINUTES);
            return now.isAfter(expiryTime);
        });
    }

    // Koliko je ostalo pokusaja
    public int getRemainingAttempts(String ipAddress) {
        LoginAttempt attempt = attemptCache.get(ipAddress);
        if (attempt == null) {
            return MAX_ATTEMPTS;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = attempt.firstAttempt.plusMinutes(TIME_WINDOW_MINUTES);

        if (now.isAfter(expiryTime)) {
            // Istekao vremenski period
            return MAX_ATTEMPTS;
        }

        return Math.max(0, MAX_ATTEMPTS - attempt.count);
    }
}