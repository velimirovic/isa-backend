package com.example.jutjubic.core.service;

import com.example.jutjubic.api.dto.CommentResponseDTO;
import com.example.jutjubic.api.dto.CreateCommentDTO;
import org.springframework.data.domain.Page;

//Servis za upravljanje komentarima

public interface CommentService {

    CommentResponseDTO createComment(Long videoId, CreateCommentDTO commentDTO, String userEmail);
    Page<CommentResponseDTO> getCommentsByVideo(Long videoId, int page, int size);
    void deleteComment(Long commentId, String userEmail);
}