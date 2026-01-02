package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.api.dto.videopost.VideoPostDraftDTO;
import com.example.jutjubic.api.dto.videopost.VideoResponseDTO;
import com.example.jutjubic.core.service.FileStoringService;
import com.example.jutjubic.core.service.VideoPostService;
import com.example.jutjubic.domain.videopost.VideoPostStatus;
import com.example.jutjubic.infrastructure.persistence.entity.UserEntity;
import com.example.jutjubic.infrastructure.persistence.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.persistence.repository.JpaUserRepository;
import com.example.jutjubic.infrastructure.persistence.repository.JpaVideoPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VideoPostServiceImpl implements VideoPostService {

    private final JpaVideoPostRepository videoPostRepository;
    private final JpaUserRepository userRepository;
    private final FileStoringService storingService;

    public VideoPostServiceImpl(JpaVideoPostRepository VideoPostRepository, FileStoringService storingService, JpaUserRepository userRepository) {
        this.videoPostRepository = VideoPostRepository;
        this.storingService = storingService;
        this.userRepository = userRepository;
    }

    @Transactional
    public VideoPostDraftDTO createDraft(String authorEmail) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(authorEmail);
        VideoPostEntity videoPost;
        VideoPostDraftDTO response = new VideoPostDraftDTO();
        if (userOpt.isPresent()) {
            videoPost = videoPostRepository.findDraftByAuthor(userOpt.get());
            if (videoPost == null) {
                videoPost = new VideoPostEntity();
                videoPost.setAuthor(userOpt.get());
            }
            else {
                response.setVideoPath(videoPost.getVideoPath());
                response.setThumbnailPath(videoPost.getThumbnailPath());
                response.setTitle(videoPost.getTitle());
                response.setDescription(videoPost.getDescription());
                response.setDraftId(videoPost.getDraftId());

                return response;
            }
        }
        else
            throw new RuntimeException("User not found: " + authorEmail);

        String draftId = UUID.randomUUID().toString();
        videoPost.setDraftId(draftId);
        videoPost.setStatus(VideoPostStatus.DRAFT);
        videoPostRepository.save(videoPost);
        response.setDraftId(draftId);

        return response;
    }

    @Transactional
    public String uploadVideo(MultipartFile video, String draftId) {
        if (!storingService.isFileExtensionValid(video, "video"))
            throw new RuntimeException("File extension not valid");

        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);

        if (videoPost.getVideoPath() != null && !videoPost.getVideoPath().isEmpty()) {
            storingService.deleteFile(videoPost.getVideoPath());
        }

        String videoPath = storingService.storeFile(video, Optional.of(draftId));
        videoPost.setVideoPath(videoPath);
        return videoPath;
    }

    @Transactional
    public String uploadThumbnail(MultipartFile thumbnail, String draftId) {
        if (!storingService.isFileExtensionValid(thumbnail, "image"))
            throw new RuntimeException("File extension not valid");

        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);

        if (videoPost.getThumbnailPath() != null && !videoPost.getThumbnailPath().isEmpty()) {
            storingService.deleteFile(videoPost.getThumbnailPath());
        }

        String thumbnailPath = storingService.storeFile(thumbnail, Optional.of(draftId));
        videoPost.setThumbnailPath(thumbnailPath);
        return thumbnailPath;
    }

    @Transactional
    public String uploadPostDetails(String title, String description, String draftId) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        videoPost.setTitle(title);
        videoPost.setDescription(description);

        return "success";
    }

    @Transactional
    public VideoResponseDTO publishVideoPost(String draftId) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        var result = ValidateVideoPost(videoPost);

        if (!result.isEmpty())
            throw new RuntimeException(result);

        videoPost.setStatus(VideoPostStatus.PUBLISHED);
        videoPost.setCreatedAt(LocalDateTime.now());
        videoPost.setDraftId(null);

        return mapVideoPostDTO(videoPost);
    }

    public VideoResponseDTO getVideoPost(long videoId) {
        VideoPostEntity videoPost = videoPostRepository.findById(videoId);
        if (videoPost == null)
            throw new RuntimeException("Post not found");

        var result = ValidateVideoPost(videoPost);
        if (!result.isEmpty())
            throw new RuntimeException(result);

        return mapVideoPostDTO(videoPost);
    }

    public List<VideoResponseDTO> getAllVideoPosts(int page) {
        try {
            Pageable pageable = PageRequest.of(page, 6);
            Page<VideoPostEntity> allVideoPosts = videoPostRepository.findAllPublished(pageable);
            List<VideoResponseDTO> posts = new ArrayList<>();

            for (VideoPostEntity videoPost : allVideoPosts) {
                posts.add(mapVideoPostDTO(videoPost));
            }

            return posts;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred: " + e);
        }
    }

    private String ValidateVideoPost(VideoPostEntity videoPost) {
        if (videoPost.getVideoPath() == null || videoPost.getVideoPath().isEmpty())
            return "Video path is not set";
        if (videoPost.getThumbnailPath() == null || videoPost.getThumbnailPath().isEmpty())
            return "Thumbnail path is not set";
        if (videoPost.getTitle() == null || videoPost.getTitle().isEmpty())
            return "Title is not set";

        return "";
    }

    private VideoResponseDTO mapVideoPostDTO (VideoPostEntity videoPost) {
        VideoResponseDTO videoResponseDTO = new VideoResponseDTO();
        videoResponseDTO.setId(videoPost.getId());
        videoResponseDTO.setVideoPath(videoPost.getVideoPath());
        videoResponseDTO.setThumbnailPath(videoPost.getThumbnailPath());
        videoResponseDTO.setTitle(videoPost.getTitle());
        videoResponseDTO.setDescription(videoPost.getDescription());
        videoResponseDTO.setCreatedAt(videoPost.getCreatedAt());
        videoResponseDTO.setAuthorEmail(videoPost.getAuthor().getEmail());
        videoResponseDTO.setStatus(videoPost.getStatus());
        videoResponseDTO.setDraftId(videoPost.getDraftId());
        videoResponseDTO.setViewCount(videoPost.getViewCount());

        return videoResponseDTO;
    }
}
