package com.example.jutjubic.core.service;

import com.example.jutjubic.infrastructure.entity.VideoViewEntity;
import com.example.jutjubic.infrastructure.repository.JpaVideoPostRepository;
import com.example.jutjubic.infrastructure.repository.JpaVideoViewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ViewCountService {

    private final JpaVideoPostRepository videoPostRepository;
    private final JpaVideoViewRepository videoViewRepository;

    public ViewCountService(
            JpaVideoPostRepository videoPostRepository,
            JpaVideoViewRepository videoViewRepository) {
        this.videoPostRepository = videoPostRepository;
        this.videoViewRepository = videoViewRepository;
    }

    /**
     * Increments view count for a video in a separate transaction.
     * Uses REQUIRES_NEW propagation to avoid @Version conflicts with the calling transaction.
     * This ensures the bulk UPDATE query runs in isolation without persistence context interference.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementViewCount(Long videoId) {
        log.debug("Incrementing view count for video ID: {}", videoId);
        
        // Atomic bulk UPDATE: SET view_count = view_count + 1
        videoPostRepository.incrementViewCount(videoId);
        
        // Log the view for analytics (append-only)
        VideoViewEntity view = new VideoViewEntity(videoId, LocalDateTime.now());
        videoViewRepository.save(view);
        
        log.debug("View count incremented and logged for video ID: {}", videoId);
    }
}
