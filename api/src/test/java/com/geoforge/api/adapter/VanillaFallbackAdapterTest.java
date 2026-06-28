package com.geoforge.api.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.concurrent.atomic.AtomicBoolean;

@Tag("integration")
@DisplayName("VanillaFallbackAdapter tests")
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

    @DisplayName("mapBlock on known block returns non-null")
    @Test
    void mapBlock_knownBlock_returnsNonNull() {
        Material result = adapter.mapBlock("stone");
        assertThat(result).isNotNull();
    }

    @DisplayName("mapBlock on unknown block returns stone")
    @Test
    void mapBlock_unknownBlock_returnsStone() {
        assertThat(adapter.mapBlock("nonexistent_block")).isEqualTo(Material.STONE);
    }

    @DisplayName("mapBiome on known biome returns non-null")
    @Test
    void mapBiome_knownBiome_returnsNonNull() {
        Biome result = adapter.mapBiome("plains");
        assertThat(result).isNotNull();
    }

    @DisplayName("mapBiome on unknown biome returns non-null fallback")
    @Test
    void mapBiome_unknownBiome_returnsNonNull() {
        Biome result = adapter.mapBiome("nonexistent_biome");
        assertThat(result).isNotNull();
    }

    @DisplayName("scheduleTask executes the runnable")
    @Test
    void scheduleTask_runsWithoutError() {
        World world = server.addSimpleWorld("test_world");
        Location loc = new Location(world, 0.0, 64.0, 0.0);
        AtomicBoolean executed = new AtomicBoolean(false);
        adapter.scheduleTask(loc, () -> executed.set(true));
        server.getScheduler().performOneTick();
        assertThat(executed.get()).isTrue();
    }

    @DisplayName("isFolia returns false in test environment")
    @Test
    void isFolia_returnsFalse() {
        assertThat(adapter.isFolia()).isFalse();
    }
}
