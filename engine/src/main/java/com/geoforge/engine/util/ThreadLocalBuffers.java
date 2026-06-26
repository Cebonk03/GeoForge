package com.geoforge.engine.util;

/**
 * Utility for per-thread scratch buffers used during chunk generation.
 *
 * <p>Provides {@link ThreadLocal} pooled {@code float[]}, {@code double[]}, and
 * {@link StringBuilder} instances. Buffers grow on demand and are recycled within
 * each thread. This avoids allocation pressure during tight generation loops
 * (e.g., 98k blocks per chunk × dozens of concurrent chunks).
 *
 * <p>The {@link #close()} method MUST be called after use to return buffers to
 * their pool. Use with try-with-resources:
 * <pre>{@code
 * try (var bufs = ThreadLocalBuffers.acquire()) {
 *     double[] weights = bufs.doubleArray(16 * 16);
 *     float[] heights = bufs.floatArray(256);
 *     // ... use buffers ...
 * }
 * }</pre>
 *
 * <p>This class is thread-safe: each thread gets independent buffers.
 * Zero {@code synchronized} blocks are used — thread confinement via ThreadLocal.
 */
public final class ThreadLocalBuffers implements AutoCloseable {

    private static final ThreadLocal<float[]> FLOAT_CACHE = ThreadLocal.withInitial(() -> new float[256]);
    private static final ThreadLocal<double[]> DOUBLE_CACHE = ThreadLocal.withInitial(() -> new double[256]);
    private static final ThreadLocal<StringBuilder> SB_CACHE = ThreadLocal.withInitial(() -> new StringBuilder(128));

    private float[] floatBuf;
    private double[] doubleBuf;
    private StringBuilder sb;

    private ThreadLocalBuffers() {
        this.floatBuf = FLOAT_CACHE.get();
        this.doubleBuf = DOUBLE_CACHE.get();
        this.sb = SB_CACHE.get();
    }

    /**
     * Acquires a set of thread-local buffers. Must be paired with {@link #close()}.
     *
     * @return a new ThreadLocalBuffers handle
     */
    public static ThreadLocalBuffers acquire() {
        return new ThreadLocalBuffers();
    }

    /**
     * Returns a {@code float[]} with at least the given size.
     * Grows the thread-local buffer if needed.
     */
    public float[] floatArray(int minSize) {
        if (floatBuf.length < minSize) {
            floatBuf = new float[minSize];
            FLOAT_CACHE.set(floatBuf);
        }
        return floatBuf;
    }

    /**
     * Returns a {@code double[]} with at least the given size.
     * Grows the thread-local buffer if needed.
     */
    public double[] doubleArray(int minSize) {
        if (doubleBuf.length < minSize) {
            doubleBuf = new double[minSize];
            DOUBLE_CACHE.set(doubleBuf);
        }
        return doubleBuf;
    }

    /**
     * Returns a {@link StringBuilder} with at least the given capacity.
     * Clears the buffer before returning (sets length to 0).
     */
    public StringBuilder stringBuilder(int capacity) {
        if (sb.capacity() < capacity) {
            sb = new StringBuilder(capacity);
            SB_CACHE.set(sb);
        } else {
            sb.setLength(0);
        }
        return sb;
    }

    @Override
    public void close() {
        // Buffers are automatically recycled via ThreadLocal references.
        // No explicit cleanup needed — the next acquire() on this thread
        // will get the same buffers from the ThreadLocal.
        this.floatBuf = null;
        this.doubleBuf = null;
        this.sb = null;
    }
}
