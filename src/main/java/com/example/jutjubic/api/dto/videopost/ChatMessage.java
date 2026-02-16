package com.example.jutjubic.api.dto.videopost;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ChatMessage {
    private String message;
    private String username;
    private String videoId;
    private Instant timestamp;

    public ChatMessage() {
        this.timestamp = Instant.now();
    }

    public ChatMessage(String message, String username, String videoId) {
        this.message = message;
        this.username = username;
        this.videoId = videoId;
        this.timestamp = Instant.now();
    }
}
