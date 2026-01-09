package com.example.jutjubic.core.service;

import com.example.jutjubic.api.dto.like.LikeResponseDTO;

public interface LikeService {


    LikeResponseDTO toggleLike(Long videoId, String userEmail);
    LikeResponseDTO getLikeStatus(Long videoId, String userEmail);
    long getLikeCount(Long videoId);
}