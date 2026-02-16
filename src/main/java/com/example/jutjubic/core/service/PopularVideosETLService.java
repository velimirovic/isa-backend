package com.example.jutjubic.core.service;

import com.example.jutjubic.infrastructure.entity.PopularVideosReportEntity;
import com.example.jutjubic.infrastructure.entity.VideoViewEntity;
import com.example.jutjubic.infrastructure.repository.JpaPopularVideosReportRepository;
import com.example.jutjubic.infrastructure.repository.JpaVideoViewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PopularVideosETLService {

    private final JpaVideoViewRepository videoViewRepository;
    private final JpaPopularVideosReportRepository reportRepository;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public PopularVideosETLService(
            JpaVideoViewRepository videoViewRepository,
            JpaPopularVideosReportRepository reportRepository) {
        this.videoViewRepository = videoViewRepository;
        this.reportRepository = reportRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void runDailyPopularityPipeline() {
        if (!schedulerEnabled) {
            log.info("Scheduler disabled on this replica, skipping ETL pipeline");
            return;
        }
        log.info("🚀 Starting ETL Pipeline for Popular Videos");
        LocalDateTime startTime = LocalDateTime.now();

        try {
            List<VideoViewEntity> views = extractViews();
            log.info("📊 Extracted {} views from last 7 days", views.size());

            Map<Long, Double> popularityScores = transformCalculatePopularity(views);
            log.info("🔄 Calculated popularity scores for {} videos", popularityScores.size());

            loadTop3Videos(popularityScores, startTime);
            log.info("💾 Loaded top 3 videos to report table");

            LocalDateTime endTime = LocalDateTime.now();
            long duration = ChronoUnit.SECONDS.between(startTime, endTime);
            log.info("✅ ETL Pipeline completed successfully in {} seconds", duration);

        } catch (Exception e) {
            log.error("❌ ETL Pipeline failed", e);
        }
    }

    private List<VideoViewEntity> extractViews() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return videoViewRepository.findViewsSince(sevenDaysAgo);
    }

    private Map<Long, Double> transformCalculatePopularity(List<VideoViewEntity> views) {
        LocalDateTime now = LocalDateTime.now();
        Map<Long, Double> scores = new HashMap<>();

        Map<Long, List<VideoViewEntity>> viewsByVideo = views.stream()
                .collect(Collectors.groupingBy(VideoViewEntity::getVideoId));

        for (Map.Entry<Long, List<VideoViewEntity>> entry : viewsByVideo.entrySet()) {
            Long videoId = entry.getKey();
            List<VideoViewEntity> videoViews = entry.getValue();

            double totalScore = 0.0;

            for (VideoViewEntity view : videoViews) {
                long daysAgo = ChronoUnit.DAYS.between(view.getViewedAt().toLocalDate(), now.toLocalDate());

                if (daysAgo <= 7) {
                    double weight = 7.0 - daysAgo + 1.0;
                    totalScore += weight;
                }
            }

            scores.put(videoId, totalScore);
        }

        return scores;
    }

    private void loadTop3Videos(Map<Long, Double> popularityScores, LocalDateTime runDate) {
        List<Map.Entry<Long, Double>> sortedVideos = popularityScores.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        int rank = 1;
        for (Map.Entry<Long, Double> entry : sortedVideos) {
            PopularVideosReportEntity report = new PopularVideosReportEntity(
                    runDate,
                    entry.getKey(),
                    rank,
                    entry.getValue()
            );
            reportRepository.save(report);
            log.info("📝 Rank {}: Video ID {} with score {}", rank, entry.getKey(), entry.getValue());
            rank++;
        }
    }

    @Transactional
    public void runPipelineManually() {
        log.info("🔧 Manual ETL Pipeline execution triggered");
        runDailyPopularityPipeline();
    }
}
