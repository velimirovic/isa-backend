package com.example.jutjubic.api.dto.videopost;

import com.example.jutjubic.core.domain.VideoPostStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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
    public String authorUsername;
    public VideoPostStatus status;
    public String draftId;
    public long viewCount;
    public List<String> tagNames;
    private long likeCount;
    private Float latitude;
    private Float longitude;
    public Long version;
    private LocalDateTime scheduledDateTime;
    private Integer durationSeconds;
}
