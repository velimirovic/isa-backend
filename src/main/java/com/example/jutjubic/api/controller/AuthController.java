package com.example.jutjubic.api.controller;

import com.example.jutjubic.api.dto.LoginRequest;
import com.example.jutjubic.api.dto.RegistrationRequest;
import com.example.jutjubic.api.dto.UserTokenState;
import com.example.jutjubic.core.service.AuthService;
import com.example.jutjubic.core.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Kontroler za autentifikaciju i registraciju
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RateLimiterService rateLimiterService;

    /*
     Registracija novog korisnika
     ------------------------------

     POST /auth/register
     {
       "email": "velimirovitsh@gmail.com",
       "username": "marko",
       "password": "Sifra123!",
       "confirmPassword": "Sifra123!",
       "firstName": "Marko",
       "lastName": "Velimirovic",
       "address": "Badovinci"
     }
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationRequest request) {
        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Registracija uspesna! Proverite email za aktivaciju naloga.");
    }

    /*
    Login korisnika
    ------------------------------

    POST /auth/login
    {
      "email": "velimirovitsh@gmail.com",
      "password": "Sifra123!"
    }

    PAZI NA RATE LIMITER (5 pokusaja po minuti)
    */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // 1. Preuzmi IP adresu korisnika
        String ipAddress = getClientIpAddress(httpRequest);

        // 2. Proveri rate limiter
        if (!rateLimiterService.isAllowed(ipAddress)) {
            int remaining = rateLimiterService.getRemainingAttempts(ipAddress);
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Previse pokusaja logovanja. Pokusajte ponovo za 1 minut. Preostalo pokušaja: " + remaining);
        }

        // 3. Pokusaj login
        try {
            UserTokenState tokenState = authService.login(request);

            // Uspesan login - resetuj rate limiter za ovu IP adresu
            rateLimiterService.resetAttempts(ipAddress);

            return ResponseEntity.ok(tokenState);

        } catch (RuntimeException e) {
            // Neuspesan login - registruj pokusaj
            rateLimiterService.registerAttempt(ipAddress);

            int remaining = rateLimiterService.getRemainingAttempts(ipAddress);

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage() + " Preostalo pokusaja: " + remaining);
        }
    }

    /*
      Aktivacija naloga

      GET /auth/activate?token=xxx
    */
    @GetMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam("token") String token) {
        authService.activateAccount(token);

        return ResponseEntity.ok("Nalog uspesno aktiviran! Mozete se prijaviti.");
    }

    /*
     Preuzimanje ip-a
     Uzima u obzir proxy servere (X-Forwarded-For header)
    */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");

        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            // Ako je zahtev prosao kroz proxy, uzmi prvu IP adresu
            return xForwardedForHeader.split(",")[0].trim();
        }

        // Inace, uzmi direktnu IP adresu
        return request.getRemoteAddr();
    }
}