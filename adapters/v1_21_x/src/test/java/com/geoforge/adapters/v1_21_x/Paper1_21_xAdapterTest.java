package com.geoforge.adapters.v1_21_x;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

@Tag("integration")
@DisplayName("Paper 1.21.x adapter tests")
class Paper1_21_xAdapterTest {

    // ---- Mockito-based tests (no MockBukkit needed) ----

    private final JavaPlugin mockPlugin = mock(JavaPlugin.class);

    @DisplayName("mapBlock('stone') returns Material.STONE (mockito)")
    @Test
    void mapBlock_stone_returnsMaterialStone_mockito() {
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> "stone".equals(id) ? Material.STONE : null,
                id -> null);
        assertThat(adapter.mapBlock("stone")).isEqualTo(Material.STONE);
    }

    @DisplayName("mapBlock unknown returns stone fallback (mockito)")
    @Test
    void mapBlock_unknown_returnsStoneFallback_mockito() {
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> null,
                id -> null);
        assertThat(adapter.mapBlock("__unknown__")).isEqualTo(Material.STONE);
    }

    @DisplayName("mapBiome unknown returns plains fallback (mockito)")
    @Test
    void mapBiome_unknown_returnsPlainsFallback_mockito() {
        var plains = mock(Biome.class);
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> null,
                id -> "plains".equals(id) ? plains : null);
        assertThat(adapter.mapBiome("__unknown__")).isSameAs(plains);
    }

    @DisplayName("mapBiome throws when plains fallback missing (mockito)")
    @Test
    void mapBiome_plainsMissing_throws_mockito() {
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> null,
                id -> null);
        assertThrows(IllegalStateException.class,
                () -> adapter.mapBiome("__unknown__"));
    }

    @DisplayName("isFolia returns false (mockito)")
    @Test
    void isFolia_returnsFalse_mockito() {
        var adapter = new Paper1_21_xAdapter(mockPlugin,
                id -> Material.STONE,
                id -> null);
        assertThat(adapter.isFolia()).isFalse();
    }

    // ---- MockBukkit-based tests ----

    private ServerMock server;
    private JavaPlugin plugin;
    private Paper1_21_xAdapter adapter;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        adapter = new Paper1_21_xAdapter(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @DisplayName("mapBlock('stone') returns Material.STONE")
    @Test
    void mapBlock_stone_returnsMaterialStone() {
        assertThat(adapter.mapBlock("stone")).isEqualTo(Material.STONE);
    }

    @DisplayName("mapBlock('grass_block') returns Material.GRASS_BLOCK")
    @Test
    void mapBlock_grassBlock_returnsMaterialGrassBlock() {
        assertThat(adapter.mapBlock("grass_block")).isEqualTo(Material.GRASS_BLOCK);
    }

    @DisplayName("mapBlock unknown returns stone fallback")
    @Test
    void mapBlock_unknown_returnsStoneFallback() {
        assertThat(adapter.mapBlock("__nonexistent_geoforge_internal__")).isEqualTo(Material.STONE);
    }

    @DisplayName("mapBiome('plains') returns non-null")
    @Test
    void mapBiome_plains_returnsNonNull() {
        Biome biome = adapter.mapBiome("plains");
        assertThat(biome).isNotNull();
    }

    @DisplayName("mapBiome('cherry_grove') returns non-null")
    @Test
    void mapBiome_cherryGrove_returnsNonNull() {
        Biome biome = adapter.mapBiome("cherry_grove");
        assertThat(biome).isNotNull();
    }

    @DisplayName("mapBiome unknown returns plains fallback")
    @Test
    void mapBiome_unknown_returnsPlainsFallback() {
        Biome biome = adapter.mapBiome("__nonexistent_biome__");
        assertThat(biome).isNotNull();
    }

    @DisplayName("isFolia returns false")
    @Test
    void isFolia_returnsFalse() {
        assertThat(adapter.isFolia()).isFalse();
    }
}
