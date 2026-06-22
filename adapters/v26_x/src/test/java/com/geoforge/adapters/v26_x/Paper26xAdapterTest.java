package com.geoforge.adapters.v26_x;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for Paper26xAdapter.
 *
 * <p>MockBukkit does not support Paper 26.x registry changes. Tests that require a live 26.x
 * registry are disabled with explanation. Only non-registry-dependent logic is tested here.
 */
class Paper26xAdapterTest {

    private final JavaPlugin mockPlugin = mock(JavaPlugin.class);

    @Disabled("Requires running Paper 26.x server with populated registry")
    @Test
    void mapBlock_stone_returnsMaterialStone() {
        var adapter = new Paper26xAdapter(mockPlugin);
        var result = adapter.mapBlock("stone");
        assertEquals(Material.STONE, result);
    }

    @Disabled("MockedStatic fails due to Bukkit static initializer; covered by integration tests")
    @Test
    void mapBlock_returnsStone_whenRegistryReturnsNull() {
        var adapter = new Paper26xAdapter(mockPlugin);
        assertEquals(Material.STONE, adapter.mapBlock("unknown_block_id"));
    }

    @Test
    void isFolia_returnsFalse() {
        var adapter = new Paper26xAdapter(mockPlugin);
        assertFalse(adapter.isFolia());
    }
}
