package com.fetisman;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class GlobalRateLimiter {
    private static final int MAX_TOKENS = 100;
    private static final Semaphore semaphore = new Semaphore(0);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static {
        // adds 1 token each 10 ms , max 100 tokens per sec.
        scheduler.scheduleAtFixedRate(() -> {
            if (semaphore.availablePermits() < MAX_TOKENS) {
                semaphore.release(); // add 1 token
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    public static void acquire() {
        try {
            semaphore.acquire(); // blocked until token appearance
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for rate limiter", e);
        }
    }

    public static void shutdown() {
        scheduler.shutdown();
    }
}
