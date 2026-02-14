package com.example.jutjubic.core.service;

import com.example.jutjubic.infrastructure.entity.PopularVideosReportEntity;
import com.example.jutjubic.infrastructure.entity.VideoViewEntity;
import com.example.jutjubic.infrastructure.repository.JpaPopularVideosReportRepository;
import com.example.jutjubic.infrastructure.repository.JpaVideoViewRepository;
import lombok.extern.slf4j.Slf4j;
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

    public PopularVideosETLService(
            JpaVideoViewRepository videoViewRepository,
            JpaPopularVideosReportRepository reportRepository) {
        this.videoViewRepository = videoViewRepository;
        this.reportRepository = reportRepository;
    }

    /**
     * ETL Pipeline koji se izvršava svaki dan u 2:00 ujutru
     * Cron format: second minute hour day month dayOfWeek
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void runDailyPopularityPipeline() {
        log.info("🚀 Starting ETL Pipeline for Popular Videos");
        LocalDateTime startTime = LocalDateTime.now();

        try {
            // EXTRACT
            List<VideoViewEntity> views = extractViews();
            log.info("📊 Extracted {} views from last 7 days", views.size());

            // TRANSFORM
            Map<Long, Double> popularityScores = transformCalculatePopularity(views);
            log.info("🔄 Calculated popularity scores for {} videos", popularityScores.size());

            // LOAD
            loadTop3Videos(popularityScores, startTime);
            log.info("💾 Loaded top 3 videos to report table");

            LocalDateTime endTime = LocalDateTime.now();
            long duration = ChronoUnit.SECONDS.between(startTime, endTime);
            log.info("✅ ETL Pipeline completed successfully in {} seconds", duration);

        } catch (Exception e) {
            log.error("❌ ETL Pipeline failed", e);
        }
    }

    /**
     * EXTRACT: Iščitavanje pregleda iz poslednih 7 dana
     */
    private List<VideoViewEntity> extractViews() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return videoViewRepository.findViewsSince(sevenDaysAgo);
    }

    /**
     * TRANSFORM: Računanje popularity score-a za svaki video
     * Formula: pregledi od pre X dana se množe sa težinom (7 - X + 1)
     * - Pregledi od juče: težina 7
     * - Pregledi od pre 2 dana: težina 6
     * - ...
     * - Pregledi od pre 7 dana: težina 1
     */
    private Map<Long, Double> transformCalculatePopularity(List<VideoViewEntity> views) {
        LocalDateTime now = LocalDateTime.now();
        Map<Long, Double> scores = new HashMap<>();

        // Grupisanje po video_id
        Map<Long, List<VideoViewEntity>> viewsByVideo = views.stream()
                .collect(Collectors.groupingBy(VideoViewEntity::getVideoId));

        // Računanje score-a za svaki video
        for (Map.Entry<Long, List<VideoViewEntity>> entry : viewsByVideo.entrySet()) {
            Long videoId = entry.getKey();
            List<VideoViewEntity> videoViews = entry.getValue();

            double totalScore = 0.0;

            for (VideoViewEntity view : videoViews) {
                // Koliko dana je prošlo od pregleda
                long daysAgo = ChronoUnit.DAYS.between(view.getViewedAt().toLocalDate(), now.toLocalDate());

                // Težina: 7 za juče, 6 za prekjuče, ..., 1 za pre 7 dana
                // Ako je daysAgo > 7, ignorišemo (ali ne bi trebalo da se desi jer extract filtrira)
                if (daysAgo <= 7) {
                    double weight = 7.0 - daysAgo + 1.0;
                    totalScore += weight;
                }
            }

            scores.put(videoId, totalScore);
        }

        return scores;
    }

    /**
     * LOAD: Upisivanje top 3 videa u report tabelu
     */
    private void loadTop3Videos(Map<Long, Double> popularityScores, LocalDateTime runDate) {
        // Sortiranje videa po score-u (opadajuće)
        List<Map.Entry<Long, Double>> sortedVideos = popularityScores.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        // Kreiranje report entiteta za top 3
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

    /**
     * Metoda za ručno pokretanje pipeline-a (za testiranje)
     * Može se pozvati preko REST endpoint-a
     */
    @Transactional
    public void runPipelineManually() {
        log.info("🔧 Manual ETL Pipeline execution triggered");
        runDailyPopularityPipeline();
    }
}
