package com.example.jutjubic.api.dto.videopost;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranscodingMessage implements Serializable {
    private String videoPath;
    private String draftId;
    private String outputResolution;
    private String codec;
    private String bitrate;
}
