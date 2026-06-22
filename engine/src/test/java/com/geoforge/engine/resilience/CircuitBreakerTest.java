package com.geoforge.engine.resilience;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest {

    @Test
    void afterThresholdFailures_fallbackInvoked() {
        var cb = new CircuitBreaker(3, 30_000L);
        var counter = new AtomicInteger(0);

        // 3 failures to trip the breaker
        for (int i = 0; i < 3; i++) {
            final int fi = i;
            cb.execute(
                    () -> {
                        throw new RuntimeException("fail " + fi);
                    },
                    () -> "fallback_" + counter.getAndIncrement());
        }

        // Next call should use fallback without attempting action
        var result =
                cb.execute(
                        () -> {
                            throw new RuntimeException("should not be called");
                        },
                        () -> "tripped");
        assertEquals("tripped", result);
    }

    @Test
    void successResetsCounter() {
        var cb = new CircuitBreaker(3, 30_000L);
        var counter = new AtomicInteger(0);

        // Two failures
        cb.execute(
                () -> {
                    throw new RuntimeException("fail1");
                },
                () -> "fb1");
        cb.execute(
                () -> {
                    throw new RuntimeException("fail2");
                },
                () -> "fb2");

        // One success — resets counter
        cb.execute(() -> "success", () -> "should_not_happen");

        // After reset, failures should be counted from 0 again
        cb.execute(
                () -> {
                    throw new RuntimeException("fail3");
                },
                () -> "fb3");
        cb.execute(
                () -> {
                    throw new RuntimeException("fail4");
                },
                () -> "fb4");

        // Only 2 failures since reset = not tripped yet
        var result =
                cb.execute(
                        () -> "still_working",
                        () -> "should_not_happen");
        assertEquals("still_working", result);
    }

    @Test
    void recoverAfterCooldown() throws InterruptedException {
        var cb = new CircuitBreaker(1, 100L);
        // Trip the breaker
        cb.execute(
                () -> {
                    throw new RuntimeException("fail");
                },
                () -> "fallback");

        // Wait for cooldown
        Thread.sleep(150L);

        // Should attempt the action again
        var result = cb.execute(() -> "recovered", () -> "should_not_happen");
        assertEquals("recovered", result);
    }

    @Test
    void constructor_throwsOnInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> new CircuitBreaker(0, 1000L));
        assertThrows(IllegalArgumentException.class, () -> new CircuitBreaker(1, 0L));
    }
}
