package com.example.jutjubic.api.controller;

import com.example.jutjubic.api.dto.videopost.VideoPostDraftDTO;
import com.example.jutjubic.api.dto.videopost.VideoResponseDTO;
import com.example.jutjubic.core.service.VideoPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
public class VideoPostController {

    final VideoPostService videoPostService;

    @PostMapping("/api/video-posts/draft")
    public ResponseEntity<VideoPostDraftDTO> startDraft(
            @RequestParam("authorEmail") String authorEmail) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(videoPostService.createDraft(authorEmail));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PostMapping("/api/video-posts/{id}/video")
    public ResponseEntity<String> uploadVideo(
            @RequestParam("video") MultipartFile video,
            @PathVariable("id") String draftId) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(videoPostService.uploadVideo(video, draftId));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e);
        }
    }

    @PostMapping("/api/video-posts/{id}/thumbnail")
    public ResponseEntity<String> uploadThumbnail(
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @PathVariable("id") String draftId) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(videoPostService.uploadThumbnail(thumbnail, draftId));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e);
        }
    }

    @PatchMapping("/api/video-posts/{id}")
    public ResponseEntity<String> uploadPostDetails(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @PathVariable("id") String draftId) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(videoPostService.uploadPostDetails(title, description, draftId));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e);
        }
    }

    @PostMapping("/api/video-posts/{id}/publish")
    public ResponseEntity<VideoResponseDTO> publishVideoPost(
            @PathVariable("id") String draftId) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(videoPostService.publishVideoPost(draftId));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/api/video-posts/{id}")
    public ResponseEntity<VideoResponseDTO> getVideoPostById(@PathVariable("id") String videoDraftId) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(videoPostService.getVideoPost(videoDraftId));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @GetMapping("/api/video-posts")
    public ResponseEntity<List<VideoResponseDTO>> getAllVideoPosts(@RequestParam("page") int page) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(videoPostService.getAllVideoPosts(page));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }
}
