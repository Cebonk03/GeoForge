package com.geoforge.engine.biome;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;
import java.util.List;

/**
 * Per-biome terrain configuration modifiers.
 *
 * <p>Each field provides a modifier that overrides or augments the global
 * {@link com.geoforge.engine.config.GeoForgeConfig} when generating terrain
 * for a specific biome. Default values represent neutral/identity modifiers
 * (no change relative to the global config).
 *
 * @param heightOffset          Y offset added to the base terrain height (blocks). 0 = no offset.
 * @param amplitudeMultiplier   Multiplier for continental height amplitude. 1.0 = no change.
 * @param caveAmplitudeModifier Multiplier for cave carving amplitude. 1.0 = no change.
 * @param treeType              Tree type identifier (empty string = use global).
 * @param surfaceBlock          Surface block material ID (empty string = use global).
 * @param subSurfaceBlock       Sub-surface block material ID (empty string = use global).
 * @param surfacePalette        Optional list of alternate surface block names for noise-driven variation.
 * @param allowFloatingPlants   Whether floating plants are permitted in this biome.
 * @param surfaceHardness       Surface block hardness factor in [0, 1]. 0.5 = default.
 * @param treeDensity           Tree density modifier (-1.0 = use global default).
 * @param minTreeHeight         Minimum tree height in blocks (0 = use global default).
 * @param maxTreeHeight         Maximum tree height in blocks (0 = use global default).
 * @param surfaceDepth          Depth of surface blocks below the top block (3 = default).
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
public record BiomeTerrainConfig(
        double heightOffset,
        double amplitudeMultiplier,
        double caveAmplitudeModifier,
        String treeType,
        String surfaceBlock,
        String subSurfaceBlock,
        List<String> surfacePalette,
        boolean allowFloatingPlants,
        double surfaceHardness,
        double treeDensity,
        int minTreeHeight,
        int maxTreeHeight,
        int surfaceDepth) {

    @Override
    public List<String> surfacePalette() {
        return Collections.unmodifiableList(surfacePalette);
    }

    /**
     * Returns a default configuration with all neutral/identity values.
     *
     * @return a new {@code BiomeTerrainConfig} with no modifications
     */
    public static BiomeTerrainConfig defaults() {
        return new BiomeTerrainConfig(
                0.0,     // heightOffset
                1.0,     // amplitudeMultiplier
                1.0,     // caveAmplitudeModifier
                "",      // treeType
                "",      // surfaceBlock
                "",      // subSurfaceBlock
                List.of(), // surfacePalette
                false,   // allowFloatingPlants
                0.5,     // surfaceHardness
                -1.0,    // treeDensity
                0,       // minTreeHeight
                0,       // maxTreeHeight
                3);      // surfaceDepth
    }
}
