package com.example.jutjubicconsumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadEventDTO {
    private String draftId;
    private String title;
    private String description;
    private String authorUsername;
    private String authorEmail;
    private long fileSizeBytes;
    private int durationSeconds;
    private String resolution;
    private String uploadTimestamp;
}
