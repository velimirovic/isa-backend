package com.example.jutjubic.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "popular_videos_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PopularVideosReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_date", nullable = false)
    private LocalDateTime runDate;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "popularity_score", nullable = false)
    private Double popularityScore;

    public PopularVideosReportEntity(LocalDateTime runDate, Long videoId, Integer rank, Double popularityScore) {
        this.runDate = runDate;
        this.videoId = videoId;
        this.rank = rank;
        this.popularityScore = popularityScore;
    }
}
