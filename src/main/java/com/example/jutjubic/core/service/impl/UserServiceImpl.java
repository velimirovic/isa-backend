package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.api.dto.user.UserProfileDTO;
import com.example.jutjubic.core.service.UserService;
import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Implementacija UserService
//Omogucava pregled profila korisnika
//Dostupno svima (i neautentifikovanim)

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JpaUserRepository userRepository;

    @Override
    public UserProfileDTO getUserProfile(String username) {
        // Pronadji korisnika po username-u
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Korisnik sa username-om '" + username + "' ne postoji"));

        // Mapiraj u DTO
        return mapToProfileDTO(user);
    }

    private UserProfileDTO mapToProfileDTO(UserEntity user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }
}