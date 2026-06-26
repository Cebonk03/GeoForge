package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FloodplainRiverCarverTest {

    @Test
    void carve_reducesDensityAtSomeCoordinates() {
        var carver = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        double nearSurface = 1.0;
        boolean carved = false;
        for (int x = 1; x <= 20 && !carved; x++) {
            for (int z = 1; z <= 20 && !carved; z++) {
                if (carver.carve(nearSurface, x, 0, z) < nearSurface) {
                    carved = true;
                }
            }
        }
        assertTrue(carved, "Floodplain carver should reduce density at some coordinates");
    }

    @Test
    void carve_noCarvingWhenNoiseIsPositive() {
        var carver = new FloodplainRiverCarver(42L, 0.01, 8, 3);
        double original = 5.0;
        double result = carver.carve(original, 0, 0, 0);
        assertEquals(original, result, 1e-12);
    }

    @Test
    void carve_doesNotAffectAir() {
        var carver = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        double result = carver.carve(-5.0, 7, 0, 11);
        assertEquals(-5.0, result, 1e-12);
    }

    @Test
    void carve_deterministic() {
        var carver = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        double r1 = carver.carve(1.0, 7, 0, 11);
        double r2 = carver.carve(1.0, 7, 0, 11);
        assertEquals(r1, r2, 1e-12);
    }

    @Test
    void carve_differentSeedsProduceDifferentCarving() {
        var carver1 = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        var carver2 = new FloodplainRiverCarver(99L, 0.1, 8, 10);
        boolean anyDiff = false;
        for (int x = 1; x <= 20 && !anyDiff; x++) {
            for (int z = 1; z <= 20 && !anyDiff; z++) {
                double r1 = carver1.carve(1.0, x, 0, z);
                double r2 = carver2.carve(1.0, x, 0, z);
                if (Math.abs(r1 - r2) > 1e-12) anyDiff = true;
            }
        }
        assertTrue(anyDiff, "Different seeds should produce different carving results");
    }

    @Test
    void carve_noCarvingWhenDepthIsZero() {
        var carver = new FloodplainRiverCarver(42L, 0.1, 0, 10);
        boolean unchanged = true;
        for (int x = 1; x <= 20; x++) {
            for (int z = 1; z <= 20; z++) {
                double result = carver.carve(1.0, x, 0, z);
                if (Math.abs(result - 1.0) > 1e-12) {
                    unchanged = false;
                }
            }
        }
        assertTrue(unchanged, "Floodplain carver with depth=0 should not alter density");
    }

    @Test
    void carve_floodplainIsWiderThanVshaped() {
        // With same depth but wider width, floodplain should carve more coordinates
        var floodplain = new FloodplainRiverCarver(42L, 0.1, 8, 20);
        var vshaped = new SimplexRiverCarver(42L, 0.1, 8, 3);

        int floodplainCarved = 0;
        int vshapedCarved = 0;
        for (int x = 1; x <= 30; x++) {
            for (int z = 1; z <= 30; z++) {
                if (floodplain.carve(1.0, x, 0, z) < 1.0) {
                    floodplainCarved++;
                }
                if (vshaped.carve(1.0, x, 0, z) < 1.0) {
                    vshapedCarved++;
                }
            }
        }
        assertTrue(floodplainCarved >= vshapedCarved,
                "Floodplain (width=20) should carve at least as many blocks as v-shaped (width=3): "
                        + "floodplain=" + floodplainCarved + " vshaped=" + vshapedCarved);
    }

    @Test
    void carve_floodplainIsShallowerThanVshaped() {
        // At the same coordinates, floodplain carving should be shallower
        // (less density reduction) than v-shaped with the same depth
        var floodplain = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        var vshaped = new SimplexRiverCarver(42L, 0.1, 8, 10);

        boolean floodplainShallower = false;
        for (int x = 1; x <= 30 && !floodplainShallower; x++) {
            for (int z = 1; z <= 30 && !floodplainShallower; z++) {
                double fp = floodplain.carve(1.0, x, 0, z);
                double vs = vshaped.carve(1.0, x, 0, z);
                if (fp < 1.0 || vs < 1.0) {
                    // At any carved coordinate, floodplain carves less (higher density)
                    if (fp > vs + 1e-12) {
                        floodplainShallower = true;
                    }
                }
            }
        }
        assertTrue(floodplainShallower,
                "Floodplain should produce shallower carving (higher density) than v-shaped at same coordinates");
    }

    @Test
    void carve_gentleBanksProduceSmoothFalloff() {
        // At the edge of a floodplain, the transition should be gradual
        var carver = new FloodplainRiverCarver(42L, 0.1, 8, 10);
        double nearSurface = 1.0;

        // Scan along a line at z=5 and find consecutive x positions where
        // carving transitions from zero to full — the density should change
        // smoothly rather than in a single step
        double prev = nearSurface;
        boolean hasTransition = false;
        boolean smoothTransition = true;
        for (int x = 1; x <= 30; x++) {
            double result = carver.carve(nearSurface, x, 0, 5);
            if (result < nearSurface && prev == nearSurface) {
                hasTransition = true;
            }
            // Check we don't jump below nearSurface - canyonDepth in one step
            if (result < nearSurface - 4.0 && prev == nearSurface) {
                smoothTransition = false;
            }
            prev = result;
        }
        assertTrue(hasTransition, "Floodplain should have edge transition zones");
        assertTrue(smoothTransition, "Floodplain banks should transition gradually, not in a single large step");
    }
}
