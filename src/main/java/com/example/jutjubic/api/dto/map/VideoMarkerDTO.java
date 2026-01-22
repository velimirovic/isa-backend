package com.example.jutjubic.api.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoMarkerDTO {
    private Long id;
    private String title;
    private String draftId;
    private Float latitude;
    private Float longitude;
    private String thumbnailPath;
    private long viewCount;
    private String authorUsername;
}