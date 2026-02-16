package com.example.jutjubic.core.service;

public interface CommentRateLimiterService {

    boolean isAllowed(String userEmail);
    void registerComment(String userEmail);
    int getRemainingComments(String userEmail);
}
