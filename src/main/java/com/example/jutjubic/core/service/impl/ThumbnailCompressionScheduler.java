package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.repository.JpaVideoPostRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ThumbnailCompressionScheduler {

    private final JpaVideoPostRepository videoPostRepository;
    private final TransactionTemplate transactionTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Path UPLOADS_DIR = Paths.get(System.getProperty("user.dir"), "uploads");

    @Scheduled(cron = "0 0 1 * * *")
    public void compressOldThumbnails() {
        logger.info("> Starting thumbnail compression job");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<VideoPostEntity> posts = videoPostRepository.findUncompressedOlderThan(cutoffDate);

        logger.info("Found {} thumbnails to compress", posts.size());

        int successCount = 0;
        int failCount = 0;

        for (VideoPostEntity post : posts) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    try {
                        compressThumbnail(post.getId());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                successCount++;
            } catch (Exception e) {
                failCount++;
                logger.error("Failed to compress thumbnail for post id={}: {}", post.getId(), e.getMessage(), e);
            }
        }

        logger.info("< Thumbnail compression completed: {} succeeded, {} failed", successCount, failCount);
    }

    private void compressThumbnail(long postId) throws Exception {
        VideoPostEntity post = videoPostRepository.findById(postId);
        String thumbnailName = post.getThumbnailPath();
        File originalFile = UPLOADS_DIR.resolve(thumbnailName).toFile();

        if (!originalFile.exists()) {
            logger.warn("Thumbnail file not found: {}, marking as compressed to skip in future", originalFile.getAbsolutePath());
            videoPostRepository.markThumbnailCompressed(postId, thumbnailName);
            return;
        }

        String compressedName = buildCompressedPath(thumbnailName);
        File compressedFile = UPLOADS_DIR.resolve(compressedName).toFile();

        Thumbnails.of(originalFile)
                .scale(1.0)
                .outputQuality(0.5)
                .toFile(compressedFile);

        videoPostRepository.markThumbnailCompressed(postId, compressedName);

        logger.info("Compressed thumbnail for post id={}: {} -> {}", post.getId(), thumbnailName, compressedName);
    }

    private String buildCompressedPath(String originalPath) {
        int lastDot = originalPath.lastIndexOf('.');
        if (lastDot == -1) {
            return originalPath + "_compressed.jpg";
        }
        return originalPath.substring(0, lastDot) + "_compressed.jpg";
    }
}
