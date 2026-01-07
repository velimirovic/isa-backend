package com.example.jutjubic.api.controller;

import com.example.jutjubic.api.dto.comment.CommentResponseDTO;
import com.example.jutjubic.api.dto.comment.CreateCommentDTO;
import com.example.jutjubic.api.dto.like.LikeResponseDTO;
import com.example.jutjubic.core.service.CommentService;
import com.example.jutjubic.core.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

//Kontroler za interakcije sa video objavama (komentari)

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@CrossOrigin
public class InteractionController {

    private final CommentService commentService;
    private final LikeService likeService;  // Dodaj u konstruktor

    /**
     * Kreiranje novog komentara
     *
     * POST /api/interactions/videos/{videoId}/comments
     *
     * Body:
     * {
     *   "content": "Odličan video!"
     * }
     */
    @PostMapping("/videos/{videoId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(
            @PathVariable Long videoId,
            @Valid @RequestBody CreateCommentDTO commentDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            CommentResponseDTO comment = commentService.createComment(
                    videoId,
                    commentDTO,
                    userDetails.getUsername()  // email iz JWT tokena
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(comment);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    /**
     * Pregled komentara za video objavu
     *
     * GET /api/interactions/videos/{videoId}/comments?page=0&size=10
     *
     * Dostupno SVIMA (i neautentifikovanim)
     */
    @GetMapping("/videos/{videoId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<CommentResponseDTO> comments = commentService.getCommentsByVideo(videoId, page, size);

            return ResponseEntity.ok(comments);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * Brisanje komentara
     *
     * DELETE /api/interactions/comments/{commentId}
     *
     * Samo autor komentara može da ga obrise
     */
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            commentService.deleteComment(commentId, userDetails.getUsername());

            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    // Toggle lajk na video (like/unlike)
    //POST /api/interactions/videos/{videoId}/like

    @PostMapping("/videos/{videoId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            LikeResponseDTO response = likeService.toggleLike(videoId, userDetails.getUsername());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // Pregled broja lajkova i statusa za video
    //GET /api/interactions/videos/{videoId}/likes

    @GetMapping("/videos/{videoId}/likes")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
            LikeResponseDTO response = likeService.getLikeStatus(videoId, userEmail);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}