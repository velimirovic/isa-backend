package com.example.jutjubic.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

//Entitet za komentare na video objavama

@Entity
@Table(name = "comments", indexes = {
        // Index za brze pretraživanje komentara po video-u i vremenu (najnoviji prvo)
        @Index(name = "idx_video_created", columnList = "video_id, created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;  // Tekst komentara (max 1000 karaktera)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;  // Korisnik koji je ostavio komentar

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private VideoPostEntity video;  // Video objava na koju se odnosi komentar

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;  // Vreme kreiranja komentara

    //Automatski postavlja vreme kreiranja pre cuvanja u bazu

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}