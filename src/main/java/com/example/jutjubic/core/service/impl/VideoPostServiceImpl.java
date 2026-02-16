package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.api.dto.videopost.UploadEventDTO;
import com.example.jutjubic.api.dto.videopost.VideoPostDraftDTO;
import com.example.jutjubic.api.dto.videopost.VideoResponseDTO;
import com.example.jutjubic.core.domain.FilterType;
import com.example.jutjubic.core.service.FileStoringService;
import com.example.jutjubic.core.service.LikeService;
import com.example.jutjubic.core.service.VideoMapService;
import com.example.jutjubic.core.service.VideoPostService;
import com.example.jutjubic.core.service.ViewCountService;
import com.example.jutjubic.core.domain.VideoPostStatus;
import com.example.jutjubic.infrastructure.entity.TagEntity;
import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.entity.VideoViewEntity;
import com.example.jutjubic.infrastructure.messaging.UploadEventProducer;
import com.example.jutjubic.infrastructure.repository.JpaTagRepository;
import com.example.jutjubic.infrastructure.repository.JpaUserRepository;
import com.example.jutjubic.infrastructure.repository.JpaVideoPostRepository;
import com.example.jutjubic.infrastructure.repository.JpaVideoViewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class VideoPostServiceImpl implements VideoPostService {

    private final JpaVideoPostRepository videoPostRepository;
    private final JpaUserRepository userRepository;
    private final FileStoringService storingService;
    private final CacheManager cacheManager;
    private final JpaTagRepository tagRepository;
    private final LikeService likeService;
    private final VideoMapService videoMapService;
    private final JpaVideoViewRepository videoViewRepository;
    private final UploadEventProducer uploadEventProducer;
    private final ViewCountService viewCountService;

    public VideoPostServiceImpl(
            JpaVideoPostRepository VideoPostRepository,
            FileStoringService storingService,
            JpaUserRepository userRepository,
            CacheManager cacheManager,
            JpaTagRepository tagRepository,
            LikeService likeService,
            VideoMapService videoMapService,
            JpaVideoViewRepository videoViewRepository,
            UploadEventProducer uploadEventProducer,
            ViewCountService viewCountService) {
        this.videoPostRepository = VideoPostRepository;
        this.storingService = storingService;
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
        this.tagRepository = tagRepository;
        this.likeService = likeService;
        this.videoMapService = videoMapService;
        this.videoViewRepository = videoViewRepository;
        this.uploadEventProducer = uploadEventProducer;
        this.viewCountService = viewCountService;
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
                response.setLatitude(videoPost.getLatitude());
                response.setLongitude(videoPost.getLongitude());

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
    public String uploadPostDetails(String title, String description, List<String> tagNames,
                                    Float latitude, Float longitude, LocalDateTime scheduledDateTime, 
                                    Integer durationSeconds, String draftId) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        if (videoPost.getStatus() == VideoPostStatus.PUBLISHED)
            throw new RuntimeException("Post already published");

        videoPost.setTitle(title);
        videoPost.setDescription(description);
        videoPost.setScheduledDateTime(scheduledDateTime);
        videoPost.setDurationSeconds(durationSeconds);
        
        log.info("uploadPostDetails - scheduledDateTime received: {}, duration: {}s", 
                 scheduledDateTime, durationSeconds);
        log.info("uploadPostDetails - videoPost.scheduledDateTime set to: {}", 
                 videoPost.getScheduledDateTime());

        if (latitude != null && longitude != null) {
            videoPost.setLatitude(latitude);
            videoPost.setLongitude(longitude);
        }

        addTagsToVideo(draftId, tagNames);
        
        videoPostRepository.save(videoPost);
        videoPostRepository.flush();
        log.info("uploadPostDetails - after save, scheduledDateTime: {}", videoPost.getScheduledDateTime());

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

        videoPostRepository.save(videoPost);

        try {
            log.info("invalidating tile cache for {} {}", videoPost.getLatitude(), videoPost.getLongitude());
            videoMapService.invalidateTileCacheForLocation(videoPost.getLatitude(), videoPost.getLongitude());
        } catch (Exception e) {
            log.error("failed to invalidate tile cache", e);
        }

        
        try {
            UploadEventDTO event = UploadEventDTO.builder()
                    .draftId(videoPost.getDraftId())
                    .title(videoPost.getTitle())
                    .description(videoPost.getDescription())
                    .authorUsername(videoPost.getAuthor().getUsername())
                    .authorEmail(videoPost.getAuthor().getEmail())
                    .fileSizeBytes(104857600L) // Default 100MB (actual size not stored in entity)
                    .durationSeconds(videoPost.getDurationSeconds() != null ? videoPost.getDurationSeconds() : 0)
                    .resolution("1920x1080") // Default resolution (not stored in entity)
                    .uploadTimestamp(videoPost.getCreatedAt().toString())
                    .build();

            uploadEventProducer.sendJsonMessage(event);
            uploadEventProducer.sendProtobufMessage(event);
            log.info("Sent upload event messages for video: {}", videoPost.getDraftId());
        } catch (Exception e) {
            log.error("Failed to send upload event messages", e);        
        }

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

        // Don't block access to scheduled videos - let the frontend handle countdown
        // This allows users to see title, description, author before the video starts

        Long videoId = videoPost.getId();
        
        // Increment view count in a separate transaction (REQUIRES_NEW)
        // This avoids @Version conflicts with the current transaction
        viewCountService.incrementViewCount(videoId);

        // Reload entity to get fresh viewCount after increment
        videoPost = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Post not found after increment"));

        return mapVideoPostDTO(videoPost);
    }

    @Override
    public VideoResponseDTO getVideoPostWithoutIncrementingViews(String videoDraftId) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(videoDraftId);
        if (videoPost == null)
            throw new RuntimeException("Post not found");

        var result = ValidateVideoPost(videoPost);
        if (!result.isEmpty())
            throw new RuntimeException(result);

        return mapVideoPostDTO(videoPost);
    }

    public List<VideoResponseDTO> getAllVideoPosts(int page, int size, FilterType filter) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoPostEntity> allVideoPosts;

        LocalDateTime now = LocalDateTime.now();
        allVideoPosts = switch (filter) {
            case LAST_30_DAYS -> {
                LocalDateTime from = now.minusDays(30);
                yield videoPostRepository.findAllByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(from, pageable);
            }
            case CURRENT_YEAR -> {
                LocalDateTime from = LocalDate.now()
                        .withDayOfYear(1)
                        .atStartOfDay();
                yield videoPostRepository.findAllByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(from, pageable);
            }
            default -> videoPostRepository.findAllPublished(pageable);
        };

        List<VideoResponseDTO> posts = new ArrayList<>();

        for (VideoPostEntity videoPost : allVideoPosts) {
            // Don't filter scheduled videos - show them all on home page
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

    /**
     * @deprecated Use ViewCountService.incrementViewCount() instead.
     * This method is kept for backwards compatibility but delegates to ViewCountService.
     */
    @Deprecated
    @Transactional
    public void incrementViewCount(Long id) {
        viewCountService.incrementViewCount(id);
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

        long likeCount = likeService.getLikeCount(videoPost.getId());
        videoResponseDTO.setLikeCount(likeCount);

        List<String> tagNames = videoPost.getTags().stream()
                .map(TagEntity::getName)
                .toList();
        videoResponseDTO.setTagNames(tagNames);

        videoResponseDTO.setLatitude(videoPost.getLatitude());
        videoResponseDTO.setLongitude(videoPost.getLongitude());

        videoResponseDTO.setVersion(videoPost.getVersion());
        videoResponseDTO.setScheduledDateTime(videoPost.getScheduledDateTime());
        videoResponseDTO.setDurationSeconds(videoPost.getDurationSeconds());
        
        log.info("mapVideoPostDTO - draftId: {}, scheduledDateTime: {}, duration: {}", 
                 videoPost.getDraftId(), videoPost.getScheduledDateTime(), videoPost.getDurationSeconds());

        return videoResponseDTO;
    }

    public List<VideoResponseDTO> getVideoPostsByUser(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<VideoPostEntity> userVideos = videoPostRepository.findByAuthorUsernameAndPublished(
                username,
                pageable
        );

        LocalDateTime now = LocalDateTime.now();
        List<VideoResponseDTO> posts = new ArrayList<>();
        for (VideoPostEntity videoPost : userVideos) {
            // Filter out videos that are scheduled but not yet available
            if (videoPost.getScheduledDateTime() != null && now.isBefore(videoPost.getScheduledDateTime())) {
                continue;
            }
            posts.add(mapVideoPostDTO(videoPost));
        }
        return posts;
    }

    public Long getPlaybackOffset(String draftId) {
        VideoPostEntity videoPost = videoPostRepository.findByDraftId(draftId);
        if (videoPost == null)
            throw new RuntimeException("Post not found");

        if (videoPost.getScheduledDateTime() == null) {
            // No scheduled time, return 0 (play from start)
            log.info("getPlaybackOffset - no scheduled time, returning 0");
            return 0L;
        }

        // Use local server time for all comparisons
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduled = videoPost.getScheduledDateTime();
        
        log.info("getPlaybackOffset - draftId: {}, now: {}, scheduled: {}", 
                 draftId, now, scheduled);

        if (now.isBefore(scheduled)) {
            // Video hasn't started yet, return negative offset (seconds until start)
            long secondsUntilStart = java.time.Duration.between(now, scheduled).getSeconds();
            log.info("getPlaybackOffset - video not started, seconds until start: {}", secondsUntilStart);
            return -secondsUntilStart;
        }

        // Video has started, calculate offset in seconds
        long offset = java.time.Duration.between(scheduled, now).getSeconds();
        log.info("getPlaybackOffset - video started, offset: {}", offset);
        return offset;
    }
}
