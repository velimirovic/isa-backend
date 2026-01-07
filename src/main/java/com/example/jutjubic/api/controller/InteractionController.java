package com.example.jutjubic.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


//Kontroler za interakcije sa video objavama (komentari, lajkovi)

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@CrossOrigin
public class InteractionController {

    /**
     * endpoint za komentarisanje
     * POST /api/interactions/videos/{videoId}/comments
     *
     * @PreAuthorize("isAuthenticated()") - samo autentifikovani!
     * Spring Security automatski vraća 401 UNAUTHORIZED ako korisnik nije prijavljen
     */
    @PostMapping("/videos/{videoId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> addComment(@PathVariable Long videoId) {
        // TODO: Implementirati u zadatku 3.6
        return ResponseEntity.ok("Komentar uspešno dodat (placeholder)");
    }

    /**
     * endpoint za lajkovanje
     *
     * POST /api/interactions/videos/{videoId}/likes
     *
     * @PreAuthorize("isAuthenticated()") - samo autentifikovani!
     * Spring Security automatski vraća 401 UNAUTHORIZED ako korisnik nije prijavljen
     */
    @PostMapping("/videos/{videoId}/likes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> toggleLike(@PathVariable Long videoId) {
        // TODO: Implementirati kasnije
        return ResponseEntity.ok("Lajk uspešno toggleovan (placeholder)");
    }
}