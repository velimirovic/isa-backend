package com.example.jutjubic.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

    @NotBlank(message = "Email je obavezan")
    @Email(message = "Email mora biti validan")
    private String email;

    @NotBlank(message = "Korisničko ime je obavezno")
    @Size(min = 3, max = 30, message = "Korisnicko ime mora biti između 3 i 30 karaktera")
    private String username;

    @NotBlank(message = "Lozinka je obavezna")
    @Size(min = 6, message = "Lozinka mora imati najmanje 6 karaktera")
    private String password;

    @NotBlank(message = "Potvrda lozinke je obavezna")
    private String confirmPassword;

    @NotBlank(message = "Ime je obavezno")
    private String firstName;

    @NotBlank(message = "Prezime je obavezno")
    private String lastName;

    private String address;
}