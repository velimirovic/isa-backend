package com.example.jutjubic.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

//DTO za prikaz profila korisnika

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;  // Datum registracije

}