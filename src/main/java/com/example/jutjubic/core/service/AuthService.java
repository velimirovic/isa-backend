package com.example.jutjubic.core.service;

import com.example.jutjubic.api.dto.auth.LoginRequest;
import com.example.jutjubic.api.dto.auth.RegistrationRequest;
import com.example.jutjubic.api.dto.auth.UserTokenState;

public interface AuthService {

    void register(RegistrationRequest request);
    void activateAccount(String token);
    UserTokenState login(LoginRequest request);
}