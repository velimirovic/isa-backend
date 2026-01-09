package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.api.dto.like.LikeResponseDTO;
import com.example.jutjubic.core.service.LikeService;
import com.example.jutjubic.infrastructure.entity.LikeEntity;
import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.repository.JpaLikeRepository;
import com.example.jutjubic.infrastructure.repository.JpaUserRepository;
import com.example.jutjubic.infrastructure.repository.JpaVideoPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeServiceImpl implements LikeService {

    private final JpaLikeRepository likeRepository;
    private final JpaVideoPostRepository videoPostRepository;
    private final JpaUserRepository userRepository;

    // Toggle lajk - ako postoji ukloni ga, ako ne postoji dodaj ga

    @Override
    @Transactional
    public LikeResponseDTO toggleLike(Long videoId, String userEmail) {
        // Pronadji korisnika
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        // Pronadji video
        VideoPostEntity video = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + videoId));

        // Proveri da li lajk postoji
        Optional<LikeEntity> existingLike = likeRepository.findByUserAndVideo(user, video);

        boolean isLiked;
        if (existingLike.isPresent()) {
            // Ako postoji, ukloni ga (unlike)
            likeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            // Ako ne postoji, dodaj ga (like)
            LikeEntity newLike = new LikeEntity();
            newLike.setUser(user);
            newLike.setVideo(video);
            likeRepository.save(newLike);
            isLiked = true;
        }

        // Vrati ažurirani status
        long likeCount = likeRepository.countByVideo(video);

        return new LikeResponseDTO(likeCount, isLiked);
    }

    // Vraca broj lajkova za video i da li je trenutni korisnik lajkova
    @Override
    public LikeResponseDTO getLikeStatus(Long videoId, String userEmail) {
        // Pronađi video
        VideoPostEntity video = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + videoId));

        // Broji lajkove
        long likeCount = likeRepository.countByVideo(video);

        // Proveri da li je korisnik lajkovao (ako je ulogovan)
        boolean isLiked = false;
        if (userEmail != null && !userEmail.isEmpty()) {
            Optional<UserEntity> user = userRepository.findByEmail(userEmail);
            if (user.isPresent()) {
                isLiked = likeRepository.existsByUserAndVideo(user.get(), video);
            }
        }

        return new LikeResponseDTO(likeCount, isLiked);
    }

    // Vraca broj lajkova
    @Override
    public long getLikeCount(Long videoId) {
        VideoPostEntity video = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + videoId));

        return likeRepository.countByVideo(video);
    }
}