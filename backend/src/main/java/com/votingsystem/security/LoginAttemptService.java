package com.votingsystem.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brute-force protection: 5 failed logins lock that account's login for 15 minutes.
 * In-memory, so it resets on restart and is per-instance. In production use Redis (or a cache with
 * expiry) so the limit is shared and the map cannot grow without bound.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private record State(int failures, Instant lockedUntil) { }

    private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();

    public boolean isLocked(String key) {
        State s = states.get(key);
        if (s == null || s.lockedUntil() == null) return false;
        if (Instant.now().isAfter(s.lockedUntil())) {
            states.remove(key);
            return false;
        }
        return true;
    }

    public void recordFailure(String key) {
        states.merge(key, new State(1, null), (old, ignored) -> {
            int failures = old.failures() + 1;
            return failures >= MAX_FAILURES
                    ? new State(failures, Instant.now().plus(LOCK_DURATION))
                    : new State(failures, null);
        });
    }

    public void reset(String key) { states.remove(key); }
}
