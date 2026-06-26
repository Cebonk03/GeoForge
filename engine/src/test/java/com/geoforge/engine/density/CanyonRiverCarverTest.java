package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CanyonRiverCarverTest {

    @Test
    void carve_reducesDensityAtSomeCoordinates() {
        var carver = new CanyonRiverCarver(42L, 0.1, 8, 10);
        double nearSurface = 1.0;
        boolean carved = false;
        for (int x = 1; x <= 20 && !carved; x++) {
            for (int z = 1; z <= 20 && !carved; z++) {
                if (carver.carve(nearSurface, x, 0, z) < nearSurface) {
                    carved = true;
                }
            }
        }
        assertTrue(carved, "Canyon carver should reduce density at some coordinates");
    }

    @Test
    void carve_noCarvingWhenNoiseIsPositive() {
        var carver = new CanyonRiverCarver(42L, 0.01, 8, 3);
        double original = 5.0;
        double result = carver.carve(original, 0, 0, 0);
        assertEquals(original, result, 1e-12);
    }

    @Test
    void carve_doesNotAffectAir() {
        var carver = new CanyonRiverCarver(42L, 0.1, 8, 10);
        double result = carver.carve(-5.0, 7, 0, 11);
        assertEquals(-5.0, result, 1e-12);
    }

    @Test
    void carve_deterministic() {
        var carver = new CanyonRiverCarver(42L, 0.1, 8, 10);
        double r1 = carver.carve(1.0, 7, 0, 11);
        double r2 = carver.carve(1.0, 7, 0, 11);
        assertEquals(r1, r2, 1e-12);
    }

    @Test
    void carve_differentSeedsProduceDifferentCarving() {
        var carver1 = new CanyonRiverCarver(42L, 0.1, 8, 10);
        var carver2 = new CanyonRiverCarver(99L, 0.1, 8, 10);
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
    void carve_differentDepthsProduceDifferentCarving() {
        var shallow = new CanyonRiverCarver(42L, 0.1, 4, 10);
        var deep = new CanyonRiverCarver(42L, 0.1, 8, 10);
        boolean anyDiff = false;
        for (int x = 1; x <= 20 && !anyDiff; x++) {
            for (int z = 1; z <= 20 && !anyDiff; z++) {
                double r1 = shallow.carve(1.0, x, 0, z);
                double r2 = deep.carve(1.0, x, 0, z);
                if (Math.abs(r1 - r2) > 1e-12) anyDiff = true;
            }
        }
        assertTrue(anyDiff, "Different canyon depths should produce different carving results");
    }

    @Test
    void carve_deepCarvingSubtractsMoreThanShallow() {
        var shallow = new CanyonRiverCarver(42L, 0.1, 4, 10);
        var deep = new CanyonRiverCarver(42L, 0.1, 8, 10);
        for (int x = 1; x <= 20; x++) {
            for (int z = 1; z <= 20; z++) {
                double r1 = shallow.carve(1.0, x, 0, z);
                double r2 = deep.carve(1.0, x, 0, z);
                // Deep carving should always subtract at least as much as shallow
                assertTrue(r1 >= r2 - 1e-12,
                        "Deep canyon carving should not produce higher density than shallow at ("
                                + x + ", " + z + "): shallow=" + r1 + " deep=" + r2);
            }
        }
    }

    @Test
    void carve_noCarvingWhenCanyonDepthIsZero() {
        var carver = new CanyonRiverCarver(42L, 0.1, 0, 10);
        boolean unchanged = true;
        for (int x = 1; x <= 20; x++) {
            for (int z = 1; z <= 20; z++) {
                double result = carver.carve(1.0, x, 0, z);
                if (Math.abs(result - 1.0) > 1e-12) {
                    unchanged = false;
                }
            }
        }
        assertTrue(unchanged, "Canyon carver with depth=0 should not alter density");
    }

    @Test
    void carve_canyonSubtractsFullDepthUniformly() {
        // For canyon, points inside the river should all have the same density reduction
        var carver = new CanyonRiverCarver(42L, 0.1, 8, 10);
        double nearSurface = 1.0;
        boolean foundRiver = false;
        double carvedDensity = Double.NaN;
        for (int x = 1; x <= 30; x++) {
            for (int z = 1; z <= 30; z++) {
                double result = carver.carve(nearSurface, x, 0, z);
                if (result < nearSurface) {
                    if (!foundRiver) {
                        carvedDensity = result;
                        foundRiver = true;
                    } else {
                        // All carved points at the same surface density should
                        // have the same carved density (flat bottom)
                        assertEquals(carvedDensity, result, 1e-12,
                                "Canyon should have uniform flat bottom at (" + x + ", " + z + ")");
                    }
                }
            }
        }
        assertTrue(foundRiver, "Need a river coordinate for canyon test");
    }
}
