package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.api.dto.videopost.VideoPostDraftDTO;
import com.example.jutjubic.api.dto.videopost.VideoResponseDTO;
import com.example.jutjubic.core.service.FileStoringService;
import com.example.jutjubic.core.service.VideoPostService;
import com.example.jutjubic.domain.videopost.VideoPostStatus;
import com.example.jutjubic.infrastructure.persistence.entity.TagEntity;
import com.example.jutjubic.infrastructure.persistence.entity.UserEntity;
import com.example.jutjubic.infrastructure.persistence.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.persistence.repository.JpaTagRepository;
import com.example.jutjubic.infrastructure.persistence.repository.JpaUserRepository;
import com.example.jutjubic.infrastructure.persistence.repository.JpaVideoPostRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VideoPostServiceImpl implements VideoPostService {

    private final JpaVideoPostRepository videoPostRepository;
    private final JpaUserRepository userRepository;
    private final FileStoringService storingService;
    private final CacheManager cacheManager;
    private final JpaTagRepository tagRepository;

    public VideoPostServiceImpl(
            JpaVideoPostRepository VideoPostRepository,
            FileStoringService storingService,
            JpaUserRepository userRepository,
            CacheManager cacheManager,
            JpaTagRepository tagRepository) {
        this.videoPostRepository = VideoPostRepository;
        this.storingService = storingService;
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
        this.tagRepository = tagRepository;
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

    @Transactional (timeout=300)
    public String uploadVideo(MultipartFile video, String draftId) {
        if (!storingService.isFileExtensionValid(video, "video"))
            throw new RuntimeException("File extension not valid");

        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        if (videoPost == null)
            throw new RuntimeException("Draft not found");
        else {
            if (videoPost.getStatus() == VideoPostStatus.PUBLISHED) {
                throw new RuntimeException("Post already published");
            }
        }

        String oldPath = videoPost.getVideoPath();
        String newVideoPath = null;

        try {
            newVideoPath = storingService.storeFile(video, Optional.of(draftId));
            videoPost.setVideoPath(newVideoPath);

            videoPostRepository.save(videoPost);
            videoPostRepository.flush();

            if (oldPath != null && !oldPath.isEmpty()) {
                storingService.deleteFile(oldPath);
            }
            return newVideoPath;
        } catch (Exception e) {
            if (newVideoPath != null) {
                storingService.deleteFile(newVideoPath);
            }
            throw new RuntimeException("Upload failed and rolled back: " + e);
        }
    }

    @Transactional(timeout=20)
    public String uploadThumbnail(MultipartFile thumbnail, String draftId) {
        if (!storingService.isFileExtensionValid(thumbnail, "image"))
            throw new RuntimeException("File extension not valid");

        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        if (videoPost == null)
            throw new RuntimeException("Draft not found");
        else {
            if (videoPost.getStatus() == VideoPostStatus.PUBLISHED)
                throw new RuntimeException("Post already published");
        }

        String oldPath = videoPost.getThumbnailPath();
        String newPath = null;

        try {
            newPath = storingService.storeFile(thumbnail, Optional.of(draftId));
            videoPost.setThumbnailPath(newPath);

            videoPostRepository.save(videoPost);
            videoPostRepository.flush();

            if (oldPath != null && !oldPath.isEmpty()) {
                storingService.deleteFile(videoPost.getThumbnailPath());
            }
            return newPath;
        } catch (Exception e) {
            if (newPath != null)
                storingService.deleteFile(newPath);
            throw new RuntimeException("Upload failed and rolled back: " + e);
        }
    }

    @Transactional
    public String uploadPostDetails(String title, String description, List<String> tagNames, String draftId) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        if (videoPost.getStatus() == VideoPostStatus.PUBLISHED)
            throw new RuntimeException("Post already published");

        videoPost.setTitle(title);
        videoPost.setDescription(description);
        addTagsToVideo(draftId, tagNames);

        return "success";
    }

    @Transactional
    public VideoResponseDTO publishVideoPost(String draftId) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        var result = ValidateVideoPost(videoPost);

        if (videoPost.getStatus() == null || videoPost.getStatus() == VideoPostStatus.PUBLISHED)
            throw new RuntimeException("Post already published");

        if (!result.isEmpty())
            throw new RuntimeException(result);

        videoPost.setStatus(VideoPostStatus.PUBLISHED);
        videoPost.setCreatedAt(LocalDateTime.now());

        return mapVideoPostDTO(videoPost);
    }

    @Transactional
    public VideoResponseDTO getVideoPost(String videoDraftId) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(videoDraftId);
        if (videoPost == null)
            throw new RuntimeException("Post not found");

        var result = ValidateVideoPost(videoPost);
        if (!result.isEmpty())
            throw new RuntimeException(result);

        this.incrementViewCount(videoPost.getId());

        var dto = mapVideoPostDTO(videoPost);
        dto.setViewCount(dto.getViewCount()+1);

        return dto;
    }

    public List<VideoResponseDTO> getAllVideoPosts(int page) {
        Pageable pageable = PageRequest.of(page, 6);
        Page<VideoPostEntity> allVideoPosts = videoPostRepository.findAllPublished(pageable);
        List<VideoResponseDTO> posts = new ArrayList<>();

        for (VideoPostEntity videoPost : allVideoPosts) {
            posts.add(mapVideoPostDTO(videoPost));
        }

        return posts;
    }

    @Cacheable("thumbnails")
    public Resource getThumbnailByDraftId(String draftId) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        if (videoPost == null)
            throw new RuntimeException("Post not found");

        return storingService.loadFile(videoPost.getThumbnailPath());
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


    @Transactional
    public void addTagsToVideo(String draftId, List<String> tagNames) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        if (videoPost == null)
            throw new RuntimeException("Post not found");

        Set<TagEntity> tags = tagNames.stream()
            .map(name -> name.toLowerCase().trim())
            .distinct()
            .map(cleanName -> {
              TagEntity existingTag = tagRepository.findByName(cleanName);
              if (existingTag != null)
                  return existingTag;

              TagEntity newTag = new TagEntity();
              newTag.setName(cleanName);
              return tagRepository.save(newTag);
            }).collect(Collectors.toSet());

        videoPost.setTags(tags);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        videoPostRepository.incrementViewCount(id);
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
        videoResponseDTO.setAuthorUsername(videoPost.getAuthor().getUsername());
        videoResponseDTO.setStatus(videoPost.getStatus());
        videoResponseDTO.setDraftId(videoPost.getDraftId());
        videoResponseDTO.setViewCount(videoPost.getViewCount());

        List<String> tagNames = videoPost.getTags().stream()
                .map(TagEntity::getName)
                .toList();
        videoResponseDTO.setTagNames(tagNames);

        return videoResponseDTO;
    }
}
