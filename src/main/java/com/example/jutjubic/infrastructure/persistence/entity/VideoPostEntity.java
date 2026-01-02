package com.example.jutjubic.infrastructure.persistence.entity;

import com.example.jutjubic.domain.videopost.VideoPostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="title")
    private String title;

    @Column(name="description")
    private String description;

    @Column(name="video_path")
    private String videoPath;

    @Column(name="thumbnail_path")
    private String thumbnailPath;

    // TODO: implement tags

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="view_count")
    private long viewCount = 0L;

    @Column(name="latitude")
    private float latitude;

    @Column(name="longitude")
    private float longitude;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="author")
    private UserEntity author;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private VideoPostStatus status;

    @Column(name="draftId")
    private String draftId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
