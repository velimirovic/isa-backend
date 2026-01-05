package com.example.jutjubic.core.service;

import com.example.jutjubic.api.dto.videopost.VideoPostDraftDTO;
import com.example.jutjubic.api.dto.videopost.VideoResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoPostService {
    VideoPostDraftDTO createDraft(String authorEmail);
    String uploadVideo(MultipartFile video, String draftId);
    String uploadThumbnail(MultipartFile thumbnail, String draftId);
    String uploadPostDetails(String title, String description, List<String> tagNames,String draftId);
    VideoResponseDTO publishVideoPost(String draftId);
    VideoResponseDTO getVideoPost(String videoId);
    List<VideoResponseDTO> getAllVideoPosts(int page);
    Resource getThumbnailByDraftId(String draftId);
    void addTagsToVideo(String draftId, List<String> tagNames);
}
