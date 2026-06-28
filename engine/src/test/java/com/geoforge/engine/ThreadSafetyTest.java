package com.geoforge.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.config.GeoForgeConfig;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("threading")
@DisplayName("Thread safety tests")
class ThreadSafetyTest {

    private static final long SEED = 42L;
    private static final GeoForgeConfig CFG = GeoForgeConfig.defaults();

    @DisplayName("Concurrent getDensity calls produce no exceptions")
    @Test
    void concurrentGetDensity_noExceptions() throws Exception {
        int threadCount = 4;
        var latch = new CountDownLatch(threadCount);
        var errors = new ConcurrentLinkedQueue<Throwable>();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    var engine = new GeoForgeEngine(SEED + threadId, CFG);
                    for (int i = 0; i < 1000; i++) {
                        double d = engine.getDensity(i % 8, (i * 64) % 256, i / 8);
                        assertThat(d).isFinite();
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertTrue(errors.isEmpty(), "Thread failures: " + errors);
    }

    @DisplayName("Concurrent surface height queries produce deterministic results")
    @Test
    void concurrentSurfaceHeight_deterministic() throws Exception {
        int threadCount = 4;
        var latch = new CountDownLatch(threadCount);
        var errors = new ConcurrentLinkedQueue<String>();

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                try {
                    var engine = new GeoForgeEngine(SEED, CFG);
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int h1 = engine.getSurfaceHeight(x, z);
                            int h2 = engine.getSurfaceHeight(x, z);
                            if (h1 != h2) {
                                errors.add("Non-deterministic surface at (" + x + "," + z
                                        + "): " + h1 + " != " + h2);
                            }
                        }
                    }
                } catch (Throwable e) {
                    errors.add("Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertTrue(errors.isEmpty(), "Determinism failures: " + errors);
    }

    @DisplayName("Concurrent density and surface height calls produced no deadlock")
    @Test
    void concurrentGetDensityAndSurfaceHeight_noDeadlock() throws Exception {
        int threadCount = 8;
        var latch = new CountDownLatch(threadCount);
        var errors = new ConcurrentLinkedQueue<Throwable>();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            final boolean useDensity = t % 2 == 0;
            new Thread(() -> {
                try {
                    var engine = new GeoForgeEngine(SEED + threadId, CFG);
                    for (int i = 0; i < 500; i++) {
                        if (useDensity) {
                            engine.getDensity(i % 8, i % 256, i / 8);
                        } else {
                            engine.getSurfaceHeight(i % 16, i % 16);
                        }
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertTrue(errors.isEmpty(), "Concurrent failures: " + errors);
    }

    @DisplayName("Same seed on different threads produces same density output")
    @Test
    void sameSeed_differentThreads_sameOutput() throws Exception {
        var engine1 = new GeoForgeEngine(SEED, CFG);
        var engine2 = new GeoForgeEngine(SEED, CFG);

        var latch = new CountDownLatch(1);
        var result = new double[1];

        new Thread(() -> {
            result[0] = engine2.getDensity(10, 64, 10);
            latch.countDown();
        }).start();

        double r1 = engine1.getDensity(10, 64, 10);
        latch.await();

        assertEquals(r1, result[0], 1e-12);
    }
}
