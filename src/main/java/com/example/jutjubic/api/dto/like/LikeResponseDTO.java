package com.example.jutjubic.api.dto.like;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponseDTO {

    private long likeCount;      // Ukupan broj lajkova na videu
    private boolean isLikedByUser;  // Da li je trenutni korisnik lajkovao video
}