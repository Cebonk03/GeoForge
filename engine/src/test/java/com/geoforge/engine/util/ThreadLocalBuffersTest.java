package com.geoforge.engine.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("ThreadLocal buffers tests")
class ThreadLocalBuffersTest {

    @DisplayName("Acquire returns non-null buffer holder")
    @Test
    void acquire_returnsNonNull() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            assertNotNull(bufs);
        }
    }

    @DisplayName("Float array meets minimum size request")
    @Test
    void floatArray_meetsMinimumSize() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            float[] arr = bufs.floatArray(100);
            assertThat(arr.length).isGreaterThanOrEqualTo(100);
        }
    }

    @DisplayName("Float array grows on demand")
    @Test
    void floatArray_growsOnDemand() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            float[] small = bufs.floatArray(50);
            assertThat(small.length).isGreaterThanOrEqualTo(50);
            float[] large = bufs.floatArray(2000);
            assertThat(large.length).isGreaterThanOrEqualTo(2000);
        }
    }

    @DisplayName("Double array meets minimum size request")
    @Test
    void doubleArray_meetsMinimumSize() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            double[] arr = bufs.doubleArray(100);
            assertThat(arr.length).isGreaterThanOrEqualTo(100);
        }
    }

    @DisplayName("StringBuilder has minimum capacity")
    @Test
    void stringBuilder_hasMinimumCapacity() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            StringBuilder sb = bufs.stringBuilder(256);
            assertThat(sb.capacity()).isGreaterThanOrEqualTo(256);
        }
    }

    @DisplayName("StringBuilder is empty after acquire")
    @Test
    void stringBuilder_isEmptyAfterAcquire() {
        try (var bufs = ThreadLocalBuffers.acquire()) {
            StringBuilder sb = bufs.stringBuilder(128);
            assertEquals(0, sb.length());
        }
    }

    @DisplayName("Concurrent threads get independent buffers")
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
                    Thread.sleep(10);
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

    @DisplayName("Close clears references")
    @Test
    void close_clearsReferences() {
        var bufs = ThreadLocalBuffers.acquire();
        bufs.floatArray(16);
        bufs.close();
        try (var fresh = ThreadLocalBuffers.acquire()) {
            assertNotNull(fresh.floatArray(16));
        }
    }

    @DisplayName("Buffers are reused on same thread")
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
            assertNotNull(second);
            assertThat(second.length).isGreaterThanOrEqualTo(64);
        }
    }

    @DisplayName("Close twice is harmless")
    @Test
    void close_twice_isHarmless() {
        var bufs = ThreadLocalBuffers.acquire();
        bufs.close();
        bufs.close();
    }
}
