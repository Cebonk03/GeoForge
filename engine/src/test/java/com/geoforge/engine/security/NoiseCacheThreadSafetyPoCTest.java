package com.geoforge.engine.security;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.noise.NoiseCache;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

/**
 * PoC tests for {@link NoiseCache} thread safety under concurrent access.
 *
 * <p>Attack vector: On Folia, multiple region threads may simultaneously access the
 * {@code ThreadLocal<NoiseCache>} in GeoForgeGenerator. Each thread gets its own
 * NoiseCache instance by construction, but improper use of the cached array could
 * lead to data races.
 */
class NoiseCacheThreadSafetyPoCTest {

    /**
     * PoC: Concurrent access to separate NoiseCache instances (simulating Folia's
     * per-region threads) must not cause cross-thread interference.
     *
     * <p>Each NoiseCache owns its own float[256] array. Under ThreadLocal,
     * each thread gets a separate instance. This test verifies that concurrent
     * writes are isolated.
     */
    @Test
    void concurrentAccess_noCrossThreadInterference() throws Exception {
        int threadCount = 4;
        var executor = Executors.newFixedThreadPool(threadCount);
        var barrier = new CyclicBarrier(threadCount);
        var results = new ConcurrentHashMap<Integer, float[]>();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    barrier.await(); // All threads start simultaneously
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // Each thread creates its own NoiseCache (as ThreadLocal would)
                NoiseCache cache = new NoiseCache();
                float[] hm = cache.heightmap();

                // Fill with thread-specific pattern
                for (int i = 0; i < hm.length; i++) {
                    hm[i] = threadId * 1000.0f + i;
                }

                // Store snapshot
                results.put(threadId, hm.clone());

                // Verify the array wasn't corrupted by other threads
                for (int i = 0; i < hm.length; i++) {
                    float expected = threadId * 1000.0f + i;
                    assertEquals(expected, hm[i], 1e-4f,
                            "Thread " + threadId + " cell " + i
                            + " corrupted by concurrent access");
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS),
                "Concurrent test timed out");

        // Verify all results are correct
        assertEquals(threadCount, results.size());
        for (int t = 0; t < threadCount; t++) {
            float[] hm = results.get(t);
            assertNotNull(hm, "Thread " + t + " should have results");
            for (int i = 0; i < hm.length; i++) {
                assertEquals(t * 1000.0f + i, hm[i], 1e-4f,
                        "Result from thread " + t + " cell " + i);
            }
        }
    }

    /**
     * PoC: reset() correctness under sequential access.
     *
     * <p>After reset(), all cells must be exactly 0.0f.
     */
    @Test
    void reset_clearsAllValues() {
        NoiseCache cache = new NoiseCache();
        float[] hm = cache.heightmap();

        // Fill with non-zero values
        for (int i = 0; i < hm.length; i++) {
            hm[i] = 42.0f;
        }

        cache.reset();

        // All values must be 0 after reset
        for (int i = 0; i < hm.length; i++) {
            assertEquals(0.0f, hm[i], 1e-6f, "Cell " + i + " should be 0 after reset");
        }
    }

    /**
     * PoC: Multiple resets are safe.
     */
    @Test
    void reset_multipleSafe() {
        NoiseCache cache = new NoiseCache();
        cache.reset(); // First reset (already zero)
        cache.reset(); // Second reset
        float[] hm = cache.heightmap();
        for (float v : hm) {
            assertEquals(0.0f, v, 1e-6f);
        }
    }

    /**
     * PoC: ThreadLocal isolation proof — verify that separate instances
     * have independent arrays.
     */
    @Test
    void separateInstances_independentArrays() {
        NoiseCache cache1 = new NoiseCache();
        NoiseCache cache2 = new NoiseCache();

        float[] hm1 = cache1.heightmap();
        float[] hm2 = cache2.heightmap();

        // Same instances — but they must be different objects
        assertNotSame(hm1, hm2,
                "Each NoiseCache must have its own heightmap array");

        hm1[0] = 123.0f;
        hm2[0] = 456.0f;

        assertEquals(123.0f, hm1[0], 1e-6f, "cache1 must not affect cache2");
        assertEquals(456.0f, hm2[0], 1e-6f, "cache2 must not affect cache1");
    }

    /**
     * PoC: The heightmap array is directly mutable — this is by design but means
     * callers must not share references across threads.
     *
     * <p>Static proof: {@code heightmap()} returns the internal array reference,
     * not a defensive copy. If a caller shared this reference across threads,
     * data races would occur. This is prevented by using {@code ThreadLocal}
     * at the call site, which ensures each thread calls heightmap() on its own
     * NoiseCache instance.
     */
    @Test
    void mutableArray_documentationOfDesignChoice() {
        NoiseCache cache = new NoiseCache();
        float[] hm1 = cache.heightmap();
        float[] hm2 = cache.heightmap();

        // Same reference — not a defensive copy
        assertSame(hm1, hm2,
                "heightmap() returns the same mutable array each time");
    }

    /**
     * FINDING: NoiseCache is safe for ThreadLocal use.
     *
     * <p>1. Each NoiseCache instance owns its own 256-element float array.
     * 2. heightmap() returns a direct reference to the internal array (no copy).
     * 3. ThreadLocal ensures one instance per thread.
     * 4. The reset() method correctly zeros all values.
     *
     * <p>The only risk would be if a caller leaked the array reference to
     * another thread. But the ThreadLocal pattern prevents this at the usage site.
     */
}
