package com.example.jutjubic.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenState {

    private String accessToken;  // Token
    private Long expiresIn;      // Vreme trajanja u milisekundama (30 minuta = 1800000)
}