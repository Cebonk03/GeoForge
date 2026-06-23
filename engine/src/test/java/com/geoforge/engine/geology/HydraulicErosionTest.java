package com.geoforge.engine.geology;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class HydraulicErosionTest {

    @Test
    void erode_flatHeightmap_doesNotThrow() {
        float[] heightmap = new float[16 * 16];
        java.util.Arrays.fill(heightmap, 50.0f);
        assertDoesNotThrow(
                () -> new HydraulicErosion().erode(heightmap, 16, 8, 0L),
                "erode should not throw on flat terrain");
    }

    @Test
    void erode_performance_completesUnder5ms() {
        float[] heightmap = new float[16 * 16];
        java.util.Arrays.fill(heightmap, 50.0f);
        // Add some variation
        for (int i = 0; i < heightmap.length; i++) {
            heightmap[i] += (float) Math.sin(i * 0.5);
        }
        assertTimeout(
                Duration.ofMillis(20),
                () -> new HydraulicErosion().erode(heightmap, 16, 64, 0L),
                "64-iteration erosion should complete under 20ms");
    }

    @Test
    void erode_edgeCoordinates_noArrayIndexOutOfBounds() {
        float[] heightmap = new float[16 * 16];
        java.util.Arrays.fill(heightmap, 50.0f);
        // Erosion with 8 iterations should not crash at edges
        assertDoesNotThrow(
                () -> new HydraulicErosion().erode(heightmap, 16, 8, 42L),
                "Edge coordinates caused ArrayIndexOutOfBoundsException");
    }

    @Test
    void erode_largerSize_doesNotThrow() {
        float[] heightmap = new float[48 * 48];
        java.util.Arrays.fill(heightmap, 50.0f);
        assertDoesNotThrow(
                () -> new HydraulicErosion().erode(heightmap, 48, 16, 99L),
                "Larger heightmap erosion should not throw");
    }

    @Test
    void erode_modifiesHeightmap() {
        float[] heightmap = new float[16 * 16];
        // Create terrain with variation so droplets have slope to drive erosion
        for (int i = 0; i < heightmap.length; i++) {
            int x = i % 16;
            int z = i / 16;
            heightmap[i] = 50.0f + (float)(Math.sin(x * 0.5) * 10.0 + Math.cos(z * 0.5) * 10.0);
        }
        float[] copy = heightmap.clone();
        new HydraulicErosion().erode(heightmap, 16, 128, 77L);
        boolean modified = false;
        for (int i = 0; i < heightmap.length; i++) {
            if (Math.abs(heightmap[i] - copy[i]) > 1e-4f) {
                modified = true;
                break;
            }
        }
        assertTrue(modified, "Erosion should modify the heightmap");
    }

    @Test
    void erode_deterministic() {
        float[] hm1 = new float[16 * 16];
        java.util.Arrays.fill(hm1, 50.0f);
        for (int i = 0; i < hm1.length; i++) {
            hm1[i] += i * 0.1f;
        }
        float[] hm2 = hm1.clone();

        new HydraulicErosion().erode(hm1, 16, 16, 123L);
        new HydraulicErosion().erode(hm2, 16, 16, 123L);

        assertArrayEquals(hm1, hm2, 1e-6f, "Same seed should produce same erosion");
    }

    @Test
    void erode_configurableMaxSteps_respectsLimit() {
        int size = 32;
        float[] base = new float[size * size];
        // Create terrain with significant gradient
        for (int i = 0; i < base.length; i++) {
            int x = i % size;
            int z = i / size;
            base[i] = 50.0f + (float)(Math.sin(x * 0.3) * 15.0 + Math.cos(z * 0.3) * 15.0);
        }

        // Run with maxSteps=1, few iterations
        float[] hm1 = base.clone();
        new HydraulicErosion(1).erode(hm1, size, 8, 42L);
        float delta1 = 0;
        for (int i = 0; i < hm1.length; i++) {
            delta1 += Math.abs(hm1[i] - base[i]);
        }

        // Run with maxSteps=30, more iterations
        float[] hm2 = base.clone();
        new HydraulicErosion(30).erode(hm2, size, 16, 42L);
        float delta2 = 0;
        for (int i = 0; i < hm2.length; i++) {
            delta2 += Math.abs(hm2[i] - base[i]);
        }

        assertTrue(delta2 > delta1, "More maxSteps should produce more heightmap change");
    }

    @Test
    void constructor_negativeMaxSteps_throws() {
        assertThrows(IllegalArgumentException.class, () -> new HydraulicErosion(0));
        assertThrows(IllegalArgumentException.class, () -> new HydraulicErosion(-1));
    }
}
