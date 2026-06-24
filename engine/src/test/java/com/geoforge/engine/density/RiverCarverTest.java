package com.geoforge.engine.density;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RiverCarverTest {

    @Test
    void noopCarver_returnsUnchanged() {
        RiverCarver carver = NoopRiverCarver.instance();
        assertEquals(0.5, carver.carve(0.5, 10, 20, 30), 1e-12);
        assertEquals(-3.0, carver.carve(-3.0, -100, 50, 200), 1e-12);
        assertEquals(0.0, carver.carve(0.0, 0, 0, 0), 1e-12);
    }

    @Test
    void noopCarver_isSingleton() {
        assertSame(NoopRiverCarver.instance(), NoopRiverCarver.instance());
    }

    @Test
    void interface_carveMethod_exists() {
        // Verify the interface contract compiles and is functional
        RiverCarver custom = (d, x, y, z) -> d * 0.5;
        assertEquals(2.0, custom.carve(4.0, 0, 0, 0), 1e-12);
    }
}
