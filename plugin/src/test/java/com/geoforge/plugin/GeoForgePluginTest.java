package com.geoforge.plugin;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.generator.ChunkGenerator;
import org.junit.jupiter.api.Test;


 * Smoke tests for GeoForgePlugin that do not require MockBukkit.
 *
 * <p>MockBukkit 4.110.0 does not support JDK 25 (the plugin module's toolchain),
 * so full lifecycle tests are not possible in this module. The adapter selection
 * logic is verified by {@link AdapterFactoryTest} and chunk generation by
 * {@link GeoForgeGeneratorTest}, both of which run on the JDK 21 build job.

class GeoForgePluginTest {

    @Test
    void pluginClass_loadable() {
        assertNotNull(GeoForgePlugin.class);
    }

    @Test
    void pluginClass_extendsJavaPlugin() {
        assertEquals("org.bukkit.plugin.java.JavaPlugin",
                GeoForgePlugin.class.getSuperclass().getName());
    }

    @Test
    void getDefaultWorldGenerator_contract() {
        // Contract: method signature accepts (String worldName, String id) and returns
        // ChunkGenerator. Full verification requires MockBukkit which runs on JDK 21.
        var method = assertDoesNotThrow(() ->
                GeoForgePlugin.class.getMethod("getDefaultWorldGenerator",
                        String.class, String.class));
        assertEquals(ChunkGenerator.class, method.getReturnType());
    }
}
