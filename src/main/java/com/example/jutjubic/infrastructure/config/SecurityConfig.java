package com.example.jutjubic.infrastructure.config;

import com.example.jutjubic.infrastructure.monitoring.ActiveUsersMetrics;
import com.example.jutjubic.infrastructure.security.CustomUserDetailsService;
import com.example.jutjubic.infrastructure.security.RestAuthenticationEntryPoint;
import com.example.jutjubic.infrastructure.security.jwt.TokenAuthenticationFilter;
import com.example.jutjubic.infrastructure.security.jwt.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Omogucava @PreAuthorize u kontrolerima
public class SecurityConfig {

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TokenUtils tokenUtils;
    
    @Autowired
    private ActiveUsersMetrics activeUsersMetrics;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Stateless sesije (REST API)
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // Handler za 401 greske
        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(restAuthenticationEntryPoint)
        );

        // Dozvole pristupa
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()  // Login/registracija dostupni svima
                .requestMatchers("/actuator/**").permitAll()  // Metrics endpoints za Prometheus
                .requestMatchers("/api/video-posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/video-posts/{id}/thumbnail").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/*/profile").permitAll() // Profili korisnika - dostupno svima
                .requestMatchers("/api/interactions/**").permitAll() // Pristup svima, ali @PreAuthorize proverava autentifikaciju
                .requestMatchers("/video-chat/**", "/video-chat").permitAll()
                .requestMatchers("/api/benchmark/**").permitAll()
                .anyRequest().authenticated()  // Sve ostalo zahteva autentifikaciju
        );

        // CORS
        http.cors(cors -> cors.configure(http));

        // Disable CSRF (za REST API)
        http.csrf(csrf -> csrf.disable());

        // Dodaj TokenAuthenticationFilter pre BasicAuthenticationFilter-a
        http.addFilterBefore(
                new TokenAuthenticationFilter(tokenUtils, userDetailsService, activeUsersMetrics),
                BasicAuthenticationFilter.class
        );

        return http.build();
    }
}