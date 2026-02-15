package com.example.jutjubic.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @Column(name = "user_email")
    private String userEmail;

    public VideoViewEntity(Long videoId, LocalDateTime viewedAt) {
        this.videoId = videoId;
        this.viewedAt = viewedAt;
    }

    public VideoViewEntity(Long videoId, LocalDateTime viewedAt, String userEmail) {
        this.videoId = videoId;
        this.viewedAt = viewedAt;
        this.userEmail = userEmail;
    }
}
