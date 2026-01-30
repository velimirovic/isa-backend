package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.core.service.VideoMapService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class VideoTileScheduler {

    private final VideoMapService videoMapService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Scheduled(cron="0 0 03 * * *")
    public void nightlyCacheRefresh() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logger.info("> nightly refresh at {}", currentTime);

        try {
            long startTime = System.currentTimeMillis();

            videoMapService.refreshAllTileCache();

            long duration = System.currentTimeMillis() - startTime;
            logger.info("< nightly cache completed in {}", duration);
        } catch (Exception e) {
            logger.error("< error during nightly cache refresh", e);
        }
    }
}
