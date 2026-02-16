package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.api.dto.videopost.TranscodingMessage;
import com.example.jutjubic.core.domain.VideoPostStatus;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.repository.JpaVideoPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class TranscodingConsumer {

    private final JpaVideoPostRepository videoPostRepository;

    public TranscodingConsumer(JpaVideoPostRepository videoPostRepository) {
        this.videoPostRepository = videoPostRepository;
    }

    @RabbitListener(queues = "video-transcoding-queue", concurrency = "2")
    @Transactional
    public void handleTranscodingMessage(TranscodingMessage message) {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Received transcoding message for draftId: {}", threadName, message.getDraftId());

        // Idempotentnost - ako je vec obradjena (redelivery scenarij), preskoci
        VideoPostEntity existingPost = videoPostRepository.findByDraftId(message.getDraftId());
        if (existingPost == null || existingPost.getStatus() == VideoPostStatus.PUBLISHED) {
            log.info("[{}] Skipping already processed or missing video for draftId: {}", threadName, message.getDraftId());
            return;
        }

        String inputPath = message.getVideoPath();
        Path inputFile = Paths.get("uploads", inputPath);
        String fileNameWithoutExt = inputPath.substring(0, inputPath.lastIndexOf('.'));
        String outputFileName = fileNameWithoutExt + "_transcoded.mp4";
        Path outputFile = Paths.get("uploads", outputFileName);

        try {
            String scale = "scale=" + message.getOutputResolution();
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputFile.toString(),
                    "-vf", scale,
                    "-c:v", message.getCodec(),
                    "-b:v", message.getBitrate(),
                    "-y",
                    outputFile.toString()
            );
            processBuilder.redirectErrorStream(true);

            log.info("[{}] Starting FFmpeg transcoding: {} -> {}", threadName, inputFile, outputFile);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[{}] FFmpeg: {}", threadName, line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("[{}] Transcoding completed successfully for draftId: {}", threadName, message.getDraftId());

                VideoPostEntity videoPost = videoPostRepository.findByDraftId(message.getDraftId());
                if (videoPost != null) {
                    videoPost.setVideoPath(outputFileName);
                    videoPost.setStatus(VideoPostStatus.PUBLISHED);
                    videoPostRepository.save(videoPost);
                    log.info("[{}] Updated video post status to PUBLISHED for draftId: {}", threadName, message.getDraftId());
                }
            } else {
                log.error("[{}] FFmpeg exited with code {} for draftId: {}", threadName, exitCode, message.getDraftId());
            }
        } catch (Exception e) {
            log.error("[{}] Transcoding failed for draftId: {}", threadName, message.getDraftId(), e);
        }
    }
}
