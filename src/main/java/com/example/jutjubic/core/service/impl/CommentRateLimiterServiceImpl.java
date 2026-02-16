package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.core.service.CommentRateLimiterService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

//sliding window algoritam za ogranicavanje komentara

@Service
public class CommentRateLimiterServiceImpl implements CommentRateLimiterService {

    // max broj komentara po korisniku u vremenskom prozoru
    private static final int MAX_COMMENTS = 60;

    // vremenski prozor u minutima (1 sat = 60 minuta)
    private static final int TIME_WINDOW_MINUTES = 60;

    private final Map<String, Deque<LocalDateTime>> commentTimestamps = new ConcurrentHashMap<>();

    //proverava da li korisnik moze da postavi novi komentar
    //uklanja zastarele timestamp-ove i proverava limit
    @Override
    public boolean isAllowed(String userEmail) {
        Deque<LocalDateTime> timestamps = commentTimestamps.get(userEmail);

        if (timestamps == null) {
            return true;
        }

        // ukloni timestamp-ove starije od 1 sat
        cleanupOldTimestamps(timestamps);

        // proveri da li je broj komentara u prozoru manji od limita
        return timestamps.size() < MAX_COMMENTS;
    }


    //registruje novi komentar za korisnika
    //dodaje trenutni timestamp u listu komentara korisnika

    @Override
    public void registerComment(String userEmail) {
        commentTimestamps.computeIfAbsent(userEmail, k -> new ConcurrentLinkedDeque<>())
                .addLast(LocalDateTime.now());
    }

    //vraca broj preostalih komentara koje korisnik moze da postavi
    @Override
    public int getRemainingComments(String userEmail) {
        Deque<LocalDateTime> timestamps = commentTimestamps.get(userEmail);

        if (timestamps == null) {
            return MAX_COMMENTS;
        }

        cleanupOldTimestamps(timestamps);

        return Math.max(0, MAX_COMMENTS - timestamps.size());
    }

    //uklanja sve timestamp-ove starije od TIME_WINDOW_MINUTES iz reda
    private void cleanupOldTimestamps(Deque<LocalDateTime> timestamps) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(TIME_WINDOW_MINUTES);

        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }
    }
}
