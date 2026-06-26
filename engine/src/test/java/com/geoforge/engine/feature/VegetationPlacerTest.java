package com.geoforge.engine.feature;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class VegetationPlacerTest {

    static class RecordingBlockSetter implements BlockSetter {
        final List<String[]> blocks = new ArrayList<>();
        @Override public void setBlock(int x, int y, int z, String materialName) {
            blocks.add(new String[]{String.valueOf(x), String.valueOf(y), String.valueOf(z), materialName});
        }
        void reset() { blocks.clear(); }
    }

    @Test
    void zeroDensity_placesNothing() {
        var veg = new VegetationPlacer(0.0);
        var setter = new RecordingBlockSetter();
        veg.place(setter, 0, 0, 63, "plains", new Random(42));
        assertTrue(setter.blocks.isEmpty(), "zero density should place nothing");
    }

    @Test
    void highDensity_placesGrassInPlains() {
        var veg = new VegetationPlacer(1.0);
        var setter = new RecordingBlockSetter();
        veg.place(setter, 0, 0, 63, "plains", new Random(42));
        assertEquals("grass", setter.blocks.get(0)[3]);
    }

    @Test
    void desert_placesDeadBushes() {
        var veg = new VegetationPlacer(1.0);
        var setter = new RecordingBlockSetter();
        veg.place(setter, 0, 0, 63, "desert", new Random(42));
        assertEquals("dead_bush", setter.blocks.get(0)[3]);
    }

    @Test
    void taiga_placesFerns() {
        var veg = new VegetationPlacer(1.0);
        var setter = new RecordingBlockSetter();
        veg.place(setter, 0, 0, 63, "taiga", new Random(42));
        assertEquals("fern", setter.blocks.get(0)[3]);
    }

    @Test
    void deterministic_sameSeedSameOutput() {
        var veg = new VegetationPlacer(0.5);
        var setter1 = new RecordingBlockSetter();
        var setter2 = new RecordingBlockSetter();
        veg.place(setter1, 10, 20, 63, "plains", new Random(42));
        veg.place(setter2, 10, 20, 63, "plains", new Random(42));
        assertEquals(setter1.blocks.size(), setter2.blocks.size());
        for (int i = 0; i < setter1.blocks.size(); i++) {
            assertArrayEquals(setter1.blocks.get(i), setter2.blocks.get(i));
        }
    }
}
