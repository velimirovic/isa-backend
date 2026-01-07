package com.example.jutjubic.api.controller;

import com.example.jutjubic.api.dto.UserProfileDTO;
import com.example.jutjubic.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//Kontroler za upravljanje korisnicima

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username) {
        try {
            UserProfileDTO profile = userService.getUserProfile(username);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }
}