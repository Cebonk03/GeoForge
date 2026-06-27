package com.geoforge.api.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.concurrent.atomic.AtomicBoolean;

class VanillaFallbackAdapterTest {

    private ServerMock server;
    private JavaPlugin plugin;
    private VanillaFallbackAdapter adapter;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        adapter = new VanillaFallbackAdapter(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void mapBlock_knownBlock_returnsNonNull() {
        Material result = adapter.mapBlock("stone");
        assertNotNull(result);
    }

    @Test
    void mapBlock_unknownBlock_returnsStone() {
        assertEquals(Material.STONE, adapter.mapBlock("nonexistent_block"));
    }

    @Test
    void mapBiome_knownBiome_returnsNonNull() {
        Biome result = adapter.mapBiome("plains");
        assertNotNull(result);
    }

    @Test
    void mapBiome_unknownBiome_returnsNonNull() {
        Biome result = adapter.mapBiome("nonexistent_biome");
        assertNotNull(result);
    }

    @Test
    void scheduleTask_runsWithoutError() {
        World world = server.addSimpleWorld("test_world");
        Location loc = new Location(world, 0.0, 64.0, 0.0);
        AtomicBoolean executed = new AtomicBoolean(false);
        adapter.scheduleTask(loc, () -> executed.set(true));
        // Advance the scheduler by 1 tick to trigger the scheduled task
        server.getScheduler().performOneTick();
        assertTrue(executed.get(), "scheduleTask should execute the runnable");
    }

    @Test
    void isFolia_returnsFalse() {
        assertFalse(adapter.isFolia());
    }
}
