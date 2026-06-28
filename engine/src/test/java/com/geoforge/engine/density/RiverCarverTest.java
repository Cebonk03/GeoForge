package com.geoforge.engine.density;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Noop river carver tests")
class RiverCarverTest {

    @DisplayName("Noop carver returns density unchanged")
    @Test
    void noopCarver_returnsUnchanged() {
        RiverCarver carver = NoopRiverCarver.instance();
        assertEquals(0.5, carver.carve(0.5, 10, 20, 30), 1e-12);
        assertEquals(-3.0, carver.carve(-3.0, -100, 50, 200), 1e-12);
        assertEquals(0.0, carver.carve(0.0, 0, 0, 0), 1e-12);
    }

    @DisplayName("Noop carver is a singleton")
    @Test
    void noopCarver_isSingleton() {
        assertSame(NoopRiverCarver.instance(), NoopRiverCarver.instance());
    }

    @DisplayName("RiverCarver functional interface compiles and works")
    @Test
    void interface_carveMethod_exists() {
        RiverCarver custom = (d, x, y, z) -> d * 0.5;
        assertEquals(2.0, custom.carve(4.0, 0, 0, 0), 1e-12);
    }
}
