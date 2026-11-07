package com.pcshop.util;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginAttemptGuard {
    private static final int MAX_FAILURES = 5;
    private static final long BLOCK_MILLIS = Duration.ofMinutes(5).toMillis();
    private static final Map<String, Attempt> ATTEMPTS = new ConcurrentHashMap<>();

    private LoginAttemptGuard() {}

    public static long blockedForMillis(String key) {
        Attempt attempt = ATTEMPTS.get(key);
        if (attempt == null || attempt.blockedUntil <= System.currentTimeMillis()) {
            if (attempt != null && attempt.blockedUntil > 0) ATTEMPTS.remove(key, attempt);
            return 0;
        }
        return attempt.blockedUntil - System.currentTimeMillis();
    }

    public static void failure(String key) {
        long now = System.currentTimeMillis();
        ATTEMPTS.compute(key, (ignored, current) -> {
            boolean expired = current != null && current.blockedUntil > 0 && current.blockedUntil <= now;
            int failures = current == null || expired ? 1 : current.failures + 1;
            long blockedUntil = failures >= MAX_FAILURES ? now + BLOCK_MILLIS : 0;
            return new Attempt(failures, blockedUntil);
        });
        if (ATTEMPTS.size() > 2_000) {
            ATTEMPTS.entrySet().removeIf(entry -> entry.getValue().blockedUntil <= now);
        }
    }

    public static void success(String key) {
        ATTEMPTS.remove(key);
    }

    private record Attempt(int failures, long blockedUntil) {}
}
