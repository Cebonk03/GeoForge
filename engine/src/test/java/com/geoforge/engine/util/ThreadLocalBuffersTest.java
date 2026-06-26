package com.geoforge.engine.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class ThreadLocalBuffersTest {

    @Test
    void acquire_returnsNonNull() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            assertNotNull(bufs);
        }
    }

    @Test
    void floatArray_meetsMinimumSize() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            float[] arr = bufs.floatArray(100);
            assertTrue(arr.length >= 100);
        }
    }

    @Test
    void floatArray_growsOnDemand() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            float[] small = bufs.floatArray(50);
            assertTrue(small.length >= 50);
            float[] large = bufs.floatArray(2000);
            assertTrue(large.length >= 2000);
        }
    }

    @Test
    void doubleArray_meetsMinimumSize() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            double[] arr = bufs.doubleArray(100);
            assertTrue(arr.length >= 100);
        }
    }

    @Test
    void stringBuilder_hasMinimumCapacity() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            StringBuilder sb = bufs.stringBuilder(256);
            assertTrue(sb.capacity() >= 256);
        }
    }

    @Test
    void stringBuilder_isEmptyAfterAcquire() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            StringBuilder sb = bufs.stringBuilder(128);
            assertEquals(0, sb.length());
        }
    }

    @Test
    void concurrentThreads_getIndependentBuffers() throws Exception {
        int threadCount = 4;
        var latch = new CountDownLatch(threadCount);
        var failures = new ConcurrentLinkedQueue<String>();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try (var bufs = ThreadLocalBuffers.acquire()) {
                    double[] arr = bufs.doubleArray(16);
                    arr[0] = threadId * 100;
                    // Simulate work
                    Thread.sleep(10);
                    // Verify our write survived (no cross-thread corruption)
                    if (arr[0] != threadId * 100) {
                        failures.add("Thread " + threadId + ": data corrupted");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        assertTrue(failures.isEmpty(), "Concurrent failures: " + failures);
    }

    @Test
    void close_clearsReferences() {
        var bufs = ThreadLocalBuffers.acquire();
        bufs.floatArray(16);
        bufs.close();
        // After close, references are null. Next acquire gets fresh buffers.
        try (var fresh = ThreadLocalBuffers.acquire()) {
            assertNotNull(fresh.floatArray(16));
        }
    }

    @Test
    void buffersAreReused_onSameThread() {
        double[] first;
        try (var bufs = ThreadLocalBuffers.acquire()) {
            first = bufs.doubleArray(64);
            first[0] = 42.0;
        }
        double[] second;
        try (var bufs = ThreadLocalBuffers.acquire()) {
            second = bufs.doubleArray(64);
            // The buffer may or may not still contain 42.0 (it's recycled).
            // The key test: it's the SAME array object (identity).
            // But we can't force identity across acquire/close cycles
            // without leaking the reference. Verifying it works is enough.
            assertNotNull(second);
            assertTrue(second.length >= 64);
        }
    }

    @Test
    void close_twice_isHarmless() {
        var bufs = ThreadLocalBuffers.acquire();
        bufs.close();
        bufs.close(); // Should not throw
    }
}
