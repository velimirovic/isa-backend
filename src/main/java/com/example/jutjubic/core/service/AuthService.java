package com.example.jutjubic.core.service;

import com.example.jutjubic.api.dto.LoginRequest;
import com.example.jutjubic.api.dto.RegistrationRequest;
import com.example.jutjubic.api.dto.UserTokenState;
import com.example.jutjubic.infrastructure.persistence.entity.ActivationTokenEntity;
import com.example.jutjubic.infrastructure.persistence.entity.UserEntity;
import com.example.jutjubic.infrastructure.persistence.repository.JpaActivationTokenRepository;
import com.example.jutjubic.infrastructure.persistence.repository.JpaUserRepository;
import com.example.jutjubic.infrastructure.security.jwt.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

// Autentifikacija i registracija
@Service
public class AuthService {

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JpaActivationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenUtils tokenUtils;

    // Kompletna registracija
    @Transactional
    public void register(RegistrationRequest request) {
        // 1. Validacija - provera da li se lozinke poklapaju
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Lozinke se ne poklapaju");
        }

        // 2. Provera da email ne postoji
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email vec postoji");
        }

        // 3. Provera da username ne postoji
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Korisnicko ime vec postoji");
        }

        // 4. Kreiranje novog korisnika
        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // Hesiranje lozinke!
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAddress(request.getAddress());
        user.setActivated(false);  // Nije aktivan!

        // 5. Cuvanje u bazu
        user = userRepository.save(user);

        // 6. Generisanje aktivacionog tokena
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);  // Vazi 24h

        ActivationTokenEntity activationToken = new ActivationTokenEntity(
                token,
                user,
                expiryDate
        );
        tokenRepository.save(activationToken);

        // 7. Slanje aktivacionog emaila
        emailService.sendActivationEmail(
                user.getEmail(),
                user.getUsername(),
                token
        );
    }

    // Aktivacija naloga
    @Transactional
    public void activateAccount(String token) {
        // 1. Pronadjii token u bazi
        ActivationTokenEntity activationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Nevazeci token"));

        // 2. Proveri da li je token istekao
        if (activationToken.isExpired()) {
            throw new RuntimeException("Token je istekao. Molimo registrujte se ponovo.");
        }

        // 3. Aktiviraj korisnika
        UserEntity user = activationToken.getUser();
        user.setActivated(true);
        userRepository.save(user);

        // 4. Obris token (vise nije potreban)
        tokenRepository.delete(activationToken);
    }

    // Login
    public UserTokenState login(LoginRequest request) {
        // 1. Pokusaj autentifikacije
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 2. Proveri da li je korisnik aktivirao nalog
            UserEntity user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Korisnik ne postoji"));

            if (!user.isActivated()) {
                throw new RuntimeException("Morate aktivirati nalog pre logovanja. Proverite email.");
            }

            // 3. Generisi JWT token
            String jwt = tokenUtils.generateToken(user.getEmail());
            Long expiresIn = tokenUtils.getExpiresIn();

            // 4. Vrati token
            return new UserTokenState(jwt, expiresIn);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Pogresan email ili lozinka");
        }
    }
}