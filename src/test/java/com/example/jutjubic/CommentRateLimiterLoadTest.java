package com.example.jutjubic;

import com.example.jutjubic.core.service.CommentRateLimiterService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentRateLimiterLoadTest {

    private static final Logger LOG = LoggerFactory.getLogger(CommentRateLimiterLoadTest.class);

    @Autowired
    private CommentRateLimiterService commentRateLimiterService;

    //test 1: Jedan korisnik pokusava da postavi 100 komentara
    //Ocekivanje: prvih 60 prolazi, ostalih 40 se blokira
    @Test
    void testSingleUserRateLimit() {
        String userEmail = "test-single@example.com";

        AtomicInteger allowed = new AtomicInteger(0);
        AtomicInteger blocked = new AtomicInteger(0);

        LOG.info("=== Test 1: Jedan korisnik salje 100 komentara (limit: 60/sat) ===");

        for (int i = 0; i < 100; i++) {
            if (commentRateLimiterService.isAllowed(userEmail)) {
                commentRateLimiterService.registerComment(userEmail);
                allowed.incrementAndGet();
            } else {
                blocked.incrementAndGet();
            }
        }

        LOG.info("Dozvoljeni: {}", allowed.get());
        LOG.info("Blokirani: {}", blocked.get());
        LOG.info("Preostalo: {}", commentRateLimiterService.getRemainingComments(userEmail));

        assertEquals(60, allowed.get(), "Tacno 60 komentara treba da bude dozvoljeno");
        assertEquals(40, blocked.get(), "Tacno 40 komentara treba da bude blokirano");
        assertEquals(0, commentRateLimiterService.getRemainingComments(userEmail),
                "Nema preostalih komentara");

        LOG.info(" Test 1 PROSAO: Rate limiter pravilno ogranicava jednog korisnika ");
    }

    //test 2: Vise korisnika istovremeno komentarise
    //svaki korisnik pokusava 80 komentara
    //ocekivanje: svaki korisnik dobije tacno 60 dozvoljenih
    @Test
    void testMultipleUsersRateLimit() throws Exception {
        int numUsers = 20;
        int commentsPerUser = 80;
        int totalRequests = numUsers * commentsPerUser;

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(numUsers);

        AtomicInteger totalAllowed = new AtomicInteger(0);
        AtomicInteger totalBlocked = new AtomicInteger(0);

        LOG.info("=== Test 2: {} korisnika x {} komentara = {} ukupno zahteva ===",
                numUsers, commentsPerUser, totalRequests);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numUsers; i++) {
            final String userEmail = "user-multi-" + i + "@example.com";

            executor.submit(() -> {
                try {
                    int userAllowed = 0;
                    int userBlocked = 0;

                    for (int j = 0; j < commentsPerUser; j++) {
                        if (commentRateLimiterService.isAllowed(userEmail)) {
                            commentRateLimiterService.registerComment(userEmail);
                            userAllowed++;
                            totalAllowed.incrementAndGet();
                        } else {
                            userBlocked++;
                            totalBlocked.incrementAndGet();
                        }
                    }

                    LOG.info("Korisnik {}: dozvoljeno={}, blokirano={}",
                            userEmail, userAllowed, userBlocked);

                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        long duration = System.currentTimeMillis() - startTime;
        double throughput = totalRequests * 1000.0 / duration;

        LOG.info("Test zavrsen za {}ms", duration);
        LOG.info("Ukupno dozvoljenih: {}", totalAllowed.get());
        LOG.info("Ukupno blokiranih: {}", totalBlocked.get());
        LOG.info("Throughput: {} zahteva/sek", String.format("%.0f", throughput));

        assertTrue(completed, "Test nije zavrsen u roku od 60 sekundi!");
        // Svaki korisnik treba da dobije tacno 60 dozvoljenih
        assertEquals(numUsers * 60, totalAllowed.get(),
                "Svaki korisnik treba da ima 60 dozvoljenih komentara");
        assertEquals(numUsers * (commentsPerUser - 60), totalBlocked.get(),
                "Ostali komentari treba da budu blokirani");
        assertEquals(totalRequests, totalAllowed.get() + totalBlocked.get(),
                "Svi zahtevi moraju biti obradjeni");

        LOG.info(" Test 2 PROSAO: Visekorisnicko ogranicavanje radi dobro ");
    }

    //test 3: ekstremno opterecenje - dokazati da sistem NE DEGRADIRA pod opterecenjem
    // 50 korisnika, svaki salje 100 zahteva, 30 threadova
    @Test
    void testSystemDoesNotDegradeUnderLoad() throws Exception {
        int numUsers = 50;
        int requestsPerUser = 100;
        int totalRequests = numUsers * requestsPerUser;
        int threadPoolSize = 30;

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch latch = new CountDownLatch(numUsers);

        AtomicInteger totalAllowed = new AtomicInteger(0);
        AtomicInteger totalBlocked = new AtomicInteger(0);

        LOG.info("=== Test 3: {} korisnika x {} zahteva = {} ukupno, {} threadova ===",
                numUsers, requestsPerUser, totalRequests, threadPoolSize);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numUsers; i++) {
            final String userEmail = "stress-user-" + i + "@example.com";

            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerUser; j++) {
                        if (commentRateLimiterService.isAllowed(userEmail)) {
                            commentRateLimiterService.registerComment(userEmail);
                            totalAllowed.incrementAndGet();
                        } else {
                            totalBlocked.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        long duration = System.currentTimeMillis() - startTime;
        double throughput = totalRequests * 1000.0 / duration;

        LOG.info("========================================");
        LOG.info("REZULTATI 3. TESTA:");
        LOG.info("Trajanje: {}ms", duration);
        LOG.info("Ukupno zahteva: {}", totalRequests);
        LOG.info("Dozvoljeni: {}", totalAllowed.get());
        LOG.info("Blokirani: {}", totalBlocked.get());
        LOG.info("Throughput: {} zahteva/sek", String.format("%.0f", throughput));
        LOG.info("========================================");

        assertTrue(completed, "3. test nije zavrsen u roku!");

        // Svaki korisnik moze da dobije max 60
        assertEquals(numUsers * 60, totalAllowed.get(),
                "Ocekivano " + (numUsers * 60) + " dozvoljenih komentara");
        assertEquals(numUsers * (requestsPerUser - 60), totalBlocked.get(),
                "Ocekivano " + (numUsers * (requestsPerUser - 60)) + " blokiranih komentara");
        assertEquals(totalRequests, totalAllowed.get() + totalBlocked.get(),
                "Svi zahtevi moraju biti obradjeni");

        // Throughput mora biti dovoljno visok - sistem ne sme degradirati
        assertTrue(throughput >= 1000.0,
                "Throughput je prenizak (" + throughput + " req/s). Sistem degradira pod opterecenjem!");

        LOG.info(" Test 3 PROSAO: Sistem NE DEGRADIRA pod ekstremnim opterecenjem!");
        LOG.info(" {} zahteva/sek - sistem je stabilan", String.format("%.0f", throughput));
    }
}
