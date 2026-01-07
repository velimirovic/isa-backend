package com.example.jutjubic.api.dto;

import com.example.jutjubic.infrastructure.persistence.entity.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

//DTO za vracanje komentara klijentu

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {

    private Long id;
    private String content;
    private LocalDateTime createdAt;

    // Informacije o autoru
    private Long authorId;
    private String authorUsername;
    private String authorFirstName;
    private String authorLastName;

    // ID video objave
    private Long videoId;


    public CommentResponseDTO(CommentEntity comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();

        this.authorId = comment.getAuthor().getId();
        this.authorUsername = comment.getAuthor().getUsername();
        this.authorFirstName = comment.getAuthor().getFirstName();
        this.authorLastName = comment.getAuthor().getLastName();

        this.videoId = comment.getVideo().getId();
    }
}