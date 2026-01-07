package com.example.jutjubic.infrastructure.repository;

import com.example.jutjubic.infrastructure.entity.LikeEntity;
import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaLikeRepository extends JpaRepository<LikeEntity, Long> {

    // Pronalazi lajk za odredjenog korisnika i video
    Optional<LikeEntity> findByUserAndVideo(UserEntity user, VideoPostEntity video);

    // Broji ukupan broj lajkova za video
    long countByVideo(VideoPostEntity video);

    // Proverava da li je korisnik lajkovao video
    boolean existsByUserAndVideo(UserEntity user, VideoPostEntity video);

    // Broji lajkove po video ID-u (efikasnija varijanta)
    @Query("SELECT COUNT(l) FROM LikeEntity l WHERE l.video.id = :videoId")
    long countByVideoId(@Param("videoId") Long videoId);

    // Proverava da li je korisnik lajkovao video po ID-evim
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
            "FROM LikeEntity l WHERE l.user.id = :userId AND l.video.id = :videoId")
    boolean existsByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") Long videoId);
}