package com.geoforge.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.generator.ChunkGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("smoke")
@DisplayName("GeoForgePlugin smoke tests")
class GeoForgePluginTest {

    @DisplayName("Plugin class is loadable")
    @Test
    void pluginClass_loadable() {
        assertThat(GeoForgePlugin.class).isNotNull();
    }

    @DisplayName("Plugin class extends JavaPlugin")
    @Test
    void pluginClass_extendsJavaPlugin() {
        assertEquals("org.bukkit.plugin.java.JavaPlugin",
                GeoForgePlugin.class.getSuperclass().getName());
    }

    @DisplayName("getDefaultWorldGenerator follows contract")
    @Test
    void getDefaultWorldGenerator_contract() {
        var method = assertDoesNotThrow(() ->
                GeoForgePlugin.class.getMethod("getDefaultWorldGenerator",
                        String.class, String.class));
        assertEquals(ChunkGenerator.class, method.getReturnType());
    }
}
