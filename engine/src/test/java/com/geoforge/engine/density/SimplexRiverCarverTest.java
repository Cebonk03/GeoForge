package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SimplexRiverCarverTest {

    @Test
    void carve_reducesDensityAtSomeCoordinates() {
        var carver = new SimplexRiverCarver(42L, 0.1, 8, 10);
        double nearSurface = 1.0;
        boolean carved = false;
        for (int x = 1; x <= 20 && !carved; x++) {
            for (int z = 1; z <= 20 && !carved; z++) {
                if (carver.carve(nearSurface, x, 0, z) < nearSurface) {
                    carved = true;
                }
            }
        }
        assertTrue(carved, "River carver should reduce density at some coordinates");
    }

    @Test
    void carve_noCarvingWhenNoiseIsPositive() {
        var carver = new SimplexRiverCarver(42L, 0.01, 8, 3);
        double original = 5.0;
        double result = carver.carve(original, 0, 0, 0);
        assertEquals(original, result, 1e-12);
    }

    @Test
    void carve_depthFactorDecreasesWithDensity() {
        var carver = new SimplexRiverCarver(42L, 0.1, 8, 10);
        double nearSurface = 1.0;
        int riverX = 0, riverZ = 0;
        boolean found = false;
        for (int x = 1; x <= 20 && !found; x++) {
            for (int z = 1; z <= 20 && !found; z++) {
                if (carver.carve(nearSurface, x, 0, z) < nearSurface) {
                    riverX = x;
                    riverZ = z;
                    found = true;
                }
            }
        }
        assertTrue(found, "Need a river coordinate for depth test");

        double surfaceValue = carver.carve(nearSurface, riverX, 0, riverZ);
        double deepValue = carver.carve(100.0, riverX, 0, riverZ);

        assertTrue(surfaceValue < nearSurface, "Near-surface density should be reduced by river carving");
        assertTrue(deepValue > surfaceValue, "Deep underground should have less carving than near surface");
        assertEquals(100.0, deepValue, 1e-9, "At density >> depth, no carving should occur");
    }

    @Test
    void carve_doesNotAffectAir() {
        var carver = new SimplexRiverCarver(42L, 0.1, 8, 10);
        double result = carver.carve(-5.0, 7, 0, 11);
        assertEquals(-5.0, result, 1e-12);
    }

    @Test
    void carve_deterministic() {
        var carver = new SimplexRiverCarver(42L, 0.1, 8, 10);
        double r1 = carver.carve(1.0, 7, 0, 11);
        double r2 = carver.carve(1.0, 7, 0, 11);
        assertEquals(r1, r2, 1e-12);
    }

    @Test
    void carve_differentSeedsProduceDifferentCarving() {
        var carver1 = new SimplexRiverCarver(42L, 0.1, 8, 10);
        var carver2 = new SimplexRiverCarver(99L, 0.1, 8, 10);
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
}
