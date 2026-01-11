package com.example.jutjubic;

import com.example.jutjubic.core.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ComponentScan(basePackages = "com.example.jutjubic")
class AuthRateLimiterLoadTest {

    private static final Logger LOG = LoggerFactory.getLogger(AuthRateLimiterLoadTest.class);

    @Autowired
    private RateLimiterService rateLimiterService;

    @Test
    void testRateLimiterUnderLoad() throws Exception {
        int numIPs = 50;
        int requestsPerIP = 10;
        int totalRequests = numIPs * requestsPerIP;

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(numIPs);

        AtomicInteger allowedRequests = new AtomicInteger(0);
        AtomicInteger blockedRequests = new AtomicInteger(0);

        LOG.info("Pokretanje load testa: {} IP adresa × {} zahteva = {} ukupno",
                numIPs, requestsPerIP, totalRequests);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numIPs; i++) {
            final String ipAddress = "192.168.1." + i;

            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerIP; j++) {
                        if (rateLimiterService.isAllowed(ipAddress)) {
                            rateLimiterService.registerAttempt(ipAddress);
                            allowedRequests.incrementAndGet();
                        } else {
                            blockedRequests.incrementAndGet();
                        }
                        Thread.sleep(5);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        long duration = System.currentTimeMillis() - startTime;

        LOG.info("Test zavrsen za {}ms", duration);
        LOG.info("Ukupno zahteva: {}", totalRequests);
        LOG.info("Dozvoljeni: {}", allowedRequests.get());
        LOG.info("Blokirani: {}", blockedRequests.get());

        double throughput = totalRequests * 1000.0 / duration;

        assertTrue(completed, "Test nije zavrsio u roku!");
        assertTrue(allowedRequests.get() >= 200, "Premalo dozvoljenih!");
        assertTrue(blockedRequests.get() >= 200, "Rate limiter ne blokira!");
        assertEquals(totalRequests, allowedRequests.get() + blockedRequests.get());
        assertTrue(throughput >= 100.0, "Throughput prenizak!");

        LOG.info("=> Sistem stabilan pod ekstremnim opterecenjem! <=");
    }
}