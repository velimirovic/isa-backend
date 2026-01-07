package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.api.dto.CommentResponseDTO;
import com.example.jutjubic.api.dto.CreateCommentDTO;
import com.example.jutjubic.core.service.CommentService;
import com.example.jutjubic.infrastructure.persistence.entity.CommentEntity;
import com.example.jutjubic.infrastructure.persistence.entity.UserEntity;
import com.example.jutjubic.infrastructure.persistence.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.persistence.repository.JpaCommentRepository;
import com.example.jutjubic.infrastructure.persistence.repository.JpaUserRepository;
import com.example.jutjubic.infrastructure.persistence.repository.JpaVideoPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final JpaCommentRepository commentRepository;
    private final JpaVideoPostRepository videoPostRepository;
    private final JpaUserRepository userRepository;

    /**
     * Kreira novi komentar
     * @CacheEvict(allEntries = true) - brise sve kesirane komentare
     *
     * kes kljucevi su oblika "videoId-page-size"
     * moramo obrisati SVE kesirane komentare da bi korisnici videli nove
     */
    @Override
    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public CommentResponseDTO createComment(Long videoId, CreateCommentDTO commentDTO, String userEmail) {

        // Pronadji video objavu
        VideoPostEntity video = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video objava sa ID " + videoId + " ne postoji"));

        // Pronadji korisnika po email-u
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // Kreiraj komentar
        CommentEntity comment = new CommentEntity();
        comment.setContent(commentDTO.getContent());
        comment.setVideo(video);
        comment.setAuthor(user);
        // createdAt se automatski postavlja u @PrePersist

        // Sacuvaj u bazu
        CommentEntity savedComment = commentRepository.save(comment);

        // Vrati DTO
        return new CommentResponseDTO(savedComment);
    }

    /**
     * Vraca komentare za video objavu sa paginacijom
     * @Cacheable - kešira rezultat po videoId + page + size
     */
    @Override
    @Cacheable(value = "comments", key = "#videoId + '-' + #page + '-' + #size")
    public Page<CommentResponseDTO> getCommentsByVideo(Long videoId, int page, int size) {

        // Proveri da li video postoji
        if (!videoPostRepository.existsById(videoId)) {
            throw new RuntimeException("Video objava sa ID " + videoId + " ne postoji");
        }

        // Kreiraj Pageable (stranica, veličina, sortiranje je u query-u)
        Pageable pageable = PageRequest.of(page, size);

        // Preuzmi komentare iz baze (sortirani po createdAt DESC)
        Page<CommentEntity> commentsPage = commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId, pageable);

        // Mapiraj u DTO
        return commentsPage.map(CommentResponseDTO::new);
    }

    /**
     * Brise komentar
     * @CacheEvict(allEntries = true) - brise sve kesirane komentare
     */
    @Override
    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public void deleteComment(Long commentId, String userEmail) {

        // Pronađi komentar
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Komentar sa ID " + commentId + " ne postoji"));

        // Proveri da li je korisnik autor komentara
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Nemate pravo da obrišete ovaj komentar");
        }

        // Obrisi komentar
        commentRepository.delete(comment);
    }
}