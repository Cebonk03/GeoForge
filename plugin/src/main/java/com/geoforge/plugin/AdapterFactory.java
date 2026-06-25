package com.geoforge.plugin;

import com.geoforge.adapters.v1_21_x.Paper1_21_xAdapter;
import com.geoforge.adapters.v26_x.Paper26xAdapter;
import com.geoforge.api.adapter.GeoForgeAdapter;
import com.geoforge.api.adapter.VanillaFallbackAdapter;
import com.geoforge.api.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Factory that selects the correct {@link GeoForgeAdapter} implementation based on the
 * running server version.
 *
 * <p>Uses a pure integer switch — no reflection, no string prefix matching. If the version
 * is unrecognised, a degraded {@link VanillaFallbackAdapter} is returned with a warning log.
 *
 * <p>Version strategy:
 * <ul>
 *   <li>{@code major >= 26} — latest Paper 26.x adapter
 *   <li>{@code major == 1 && minor == 21} — Paper 1.21.x adapter
 *   <li>{@code major == 1 && minor >= 22} — explicitly falls to fallback (no adapter yet)
 *   <li>anything else — degraded {@link VanillaFallbackAdapter}
 * </ul>
 */
public final class AdapterFactory {

    private AdapterFactory() {}

    /**
     * Creates the appropriate adapter for the current server version.
     *
     * @param plugin the plugin instance (used for logging and scheduling)
     * @return a version-appropriate adapter
     */
    public static GeoForgeAdapter create(JavaPlugin plugin) {
        ServerVersion v = ServerVersion.parse(Bukkit.getMinecraftVersion());

        if (v.major() >= 26) {
            return new Paper26xAdapter(plugin);
        }
        if (v.major() == 1 && v.minor() == 21) {
            return new Paper1_21_xAdapter(plugin);
        }
        // Paper 1.22+ — no adapter yet, falls to VanillaFallbackAdapter below
        plugin.getLogger()
                .warning(
                        "GeoForge: unrecognised server version "
                                + Bukkit.getMinecraftVersion()
                                + " — using VanillaFallbackAdapter. World generation will be"
                                + " degraded.");
        return new VanillaFallbackAdapter(plugin);
    }

    /**
     * Returns the adapter {@link Class} for a given parsed version, without needing a live
     * Bukkit server. Useful for unit tests.
     */
    public static Class<? extends GeoForgeAdapter> selectClass(ServerVersion v) {
        if (v.major() >= 26) {
            return Paper26xAdapter.class;
        }
        if (v.major() == 1 && v.minor() == 21) {
            return Paper1_21_xAdapter.class;
        }
        // Paper 1.22+ — no adapter yet, falls to VanillaFallbackAdapter below
        return VanillaFallbackAdapter.class;
    }
}
