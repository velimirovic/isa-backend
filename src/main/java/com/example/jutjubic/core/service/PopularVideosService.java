package com.example.jutjubic.core.service;

import com.example.jutjubic.api.dto.videopost.VideoResponseDTO;
import com.example.jutjubic.core.domain.VideoPostStatus;
import com.example.jutjubic.infrastructure.entity.PopularVideosReportEntity;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.repository.JpaPopularVideosReportRepository;
import com.example.jutjubic.infrastructure.repository.JpaVideoPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PopularVideosService {

    private final JpaPopularVideosReportRepository reportRepository;
    private final JpaVideoPostRepository videoPostRepository;
    private final VideoPostService videoPostService;

    public PopularVideosService(
            JpaPopularVideosReportRepository reportRepository,
            JpaVideoPostRepository videoPostRepository,
            VideoPostService videoPostService) {
        this.reportRepository = reportRepository;
        this.videoPostRepository = videoPostRepository;
        this.videoPostService = videoPostService;
    }

    /**
     * Vraća top 3 najpopularnija videa iz poslednjeg ETL run-a
     */
    public List<VideoResponseDTO> getTop3PopularVideos() {
        List<PopularVideosReportEntity> reports = reportRepository.findLatestTopVideos();
        List<VideoResponseDTO> result = new ArrayList<>();

        for (PopularVideosReportEntity report : reports) {
            Optional<VideoPostEntity> videoOpt = videoPostRepository.findById(report.getVideoId());
            
            if (videoOpt.isPresent()) {
                VideoPostEntity video = videoOpt.get();
                
                // Samo ako je video published, dodaj ga u listu
                if (video.getStatus() == VideoPostStatus.PUBLISHED) {
                    try {
                        // Koristi metodu BEZ povećavanja view count-a
                        VideoResponseDTO dto = videoPostService.getVideoPostWithoutIncrementingViews(video.getDraftId());
                        result.add(dto);
                    } catch (Exception e) {
                        log.warn("Failed to load video {} for popular list", video.getDraftId(), e);
                    }
                } else {
                    log.warn("Video {} is not published (status: {}), skipping from popular list", 
                            video.getId(), video.getStatus());
                }
            } else {
                log.warn("Video with ID {} not found in database", report.getVideoId());
            }
        }

        log.info("Returning {} popular videos", result.size());
        return result;
    }

    /**
     * Ručno pokretanje ETL pipeline-a (za testiranje/admin)
     */
    @Transactional
    public void triggerETLPipeline(PopularVideosETLService etlService) {
        etlService.runPipelineManually();
    }
}
