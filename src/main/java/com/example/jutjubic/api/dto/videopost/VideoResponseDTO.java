package com.example.jutjubic.api.dto.videopost;

import com.example.jutjubic.domain.videopost.VideoPostStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VideoResponseDTO {
    public long id;
    public String videoPath;
    public String thumbnailPath;
    public String title;
    public String description;
    public LocalDateTime createdAt;
    public String authorEmail;
    public VideoPostStatus status;
    public String draftId;
    public long viewCount;
}
