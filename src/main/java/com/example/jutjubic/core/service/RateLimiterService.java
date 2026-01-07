package com.example.jutjubic.core.service;

public interface RateLimiterService {

    boolean isAllowed(String ipAddress);
    void registerAttempt(String ipAddress);
    void resetAttempts(String ipAddress);
    int getRemainingAttempts(String ipAddress);
}