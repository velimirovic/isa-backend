package com.example.jutjubic.api.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//DTO za kreiranje novog komentara

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDTO {

    @NotBlank(message = "Sadržaj komentara ne može biti prazan")
    @Size(min = 1, max = 1000, message = "Komentar mora biti između 1 i 1000 karaktera")
    private String content;

    // videoId se dobija iz path parametra, ne iz DTO-a
    // author se dobija iz JWT tokena (autentifikovan korisnik)
}