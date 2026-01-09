package com.example.jutjubic.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "video_id"})
        },
        indexes = {
                @Index(name = "idx_video_id", columnList = "video_id"),
                @Index(name = "idx_user_id", columnList = "user_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;  // Korisnik koji je lajkovao

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private VideoPostEntity video;  // Video objava koja je lajkovana

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;  // Vreme lajkovanja

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}