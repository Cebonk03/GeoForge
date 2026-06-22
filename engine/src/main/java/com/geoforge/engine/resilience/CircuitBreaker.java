package com.geoforge.engine.resilience;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A simple circuit breaker for wrapping fallible operations.
 *
 * <p>After {@code failureThreshold} consecutive failures, the circuit opens and subsequent
 * calls invoke the fallback without attempting the action. After {@code resetCooldownMs}
 * milliseconds, the circuit allows a trial call to see if the operation has recovered.
 *
 * <p>Uses only {@link AtomicInteger} and {@link AtomicLong} for concurrency, as permitted
 * by DP-5 (no synchronized, no ReentrantLock).
 */
public final class CircuitBreaker {

    private final int failureThreshold;
    private final long resetCooldownMs;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0L);

    public CircuitBreaker(int failureThreshold, long resetCooldownMs) {
        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold must be > 0");
        }
        if (resetCooldownMs <= 0) {
            throw new IllegalArgumentException("resetCooldownMs must be > 0");
        }
        this.failureThreshold = failureThreshold;
        this.resetCooldownMs = resetCooldownMs;
    }

    /**
     * Executes the given action within the circuit breaker.
     *
     * @param action   the operation to attempt
     * @param fallback the fallback supplier if the circuit is open or the action throws
     * @param <T>      the return type
     * @return either the action's result or the fallback's result
     */
    public <T> T execute(Callable<T> action, Supplier<T> fallback) {
        boolean open =
                failureCount.get() >= failureThreshold
                        && System.currentTimeMillis() - lastFailureTime.get()
                                < resetCooldownMs;
        if (open) {
            return fallback.get();
        }
        try {
            T result = action.call();
            failureCount.set(0);
            return result;
        } catch (Exception e) {
            lastFailureTime.set(System.currentTimeMillis());
            failureCount.incrementAndGet();
            return fallback.get();
        }
    }
}
