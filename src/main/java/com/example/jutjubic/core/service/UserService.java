package com.example.jutjubic.core.service;

import com.example.jutjubic.api.dto.user.UserProfileDTO;


public interface UserService {
    UserProfileDTO getUserProfile(String username);
}