package com.example.jutjubic.api.dto.videopost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoPostDraftDTO {
    public String videoPath;
    public String thumbnailPath;
    public String title;
    public String description;
    public String draftId;
}
