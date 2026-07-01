package com.geoforge.engine;

import com.geoforge.engine.config.biome.BiomeDefinition;

/**
 * Per-column pre-computed context used by {@link GeoForgeEngine#getDensity}.
 *
 * <p>Computing these values once per column (rather than once per block) eliminates
 * redundant work across the 200+ Y samples in a column. The factory method
 * {@link #compute(GeoForgeEngine, int, int)} samples biome at the column's actual
 * surface height using the domain-warped height function, which also fixes a
 * pre-existing bug where biome modifiers were sampled at whichever Y happened to
 * be queried first (often deep underground during binary search).
 *
 * @param targetHeight       the domain-warped surface height at this column
 * @param valleyFactor       river carving weight in {@code [0, 1]}; 1 = valley bottom
 * @param biomeId            resolved biome ID at the column's surface height
 * @param caveModifier       per-biome cave amplitude multiplier
 * @param heightOffset       per-biome height offset added to terrain density
 * @param amplitudeMultiplier per-biome amplitude multiplier for terrain height
 */
public record ColumnContext(
        double targetHeight,
        double valleyFactor,
        String biomeId,
        double caveModifier,
        double heightOffset,
        double amplitudeMultiplier) {

    /** Epsilon for floating-point comparisons. */
    private static final double EPSILON = 1e-12;

    /**
     * Computes the column context for the given block coordinates.
     *
     * <p>Samples biome at the surface height determined by the engine's
     * binary-search method, ensuring biome modifiers reflect the column's
     * actual surface biome (fixes the Y-bug).
     *
     * @param engine the generation engine (provides noise, biome registry, config)
     * @param blockX the x-coordinate in block space
     * @param blockZ the z-coordinate in block space
     * @return a fully populated ColumnContext
     */
    public static ColumnContext compute(GeoForgeEngine engine, int blockX, int blockZ) {
        // Sample domain-warped height (same function used by getDensity)
        double targetHeight = engine.getWarpedHeightAt(blockX, blockZ);

        // Compute terrain gradient for topographic river carving
        double hDx = engine.getWarpedHeightAt(blockX + 5, blockZ);
        double hDz = engine.getWarpedHeightAt(blockX, blockZ + 5);
        double gradX = (hDx - targetHeight) / 5.0;
        double gradZ = (hDz - targetHeight) / 5.0;
        double gradientMag = Math.sqrt(gradX * gradX + gradZ * gradZ);
        double valleyFactor = Math.max(0.0, Math.min(1.0, 1.0 - gradientMag * 2.0));
        // Resolve biome at approximate surface height (targetHeight ≈ surfaceY)
        // This fixes the Y-bug where biome was sampled at an arbitrary query Y
        // (e.g., Y=-59 during binary search) instead of the actual surface.
        // Using targetHeight avoids calling getSurfaceHeight() which would
        // create circular recursion with getDensity().
        int sampleY = (int) Math.round(Math.max(engine.config().minHeight(),
                Math.min(engine.config().maxHeight() - 1, targetHeight)));
        String biomeId = engine.getBiomeId(blockX, sampleY, blockZ);

        // Extract biome terrain modifiers
        BiomeDefinition def = engine.getBiomeRegistry().forBiome(biomeId);
        double caveModifier = Math.abs(def.caveAmplitudeModifier() - 1.0) > EPSILON
                ? def.caveAmplitudeModifier() : 1.0;
        double heightOffset = Math.abs(def.heightOffset()) > EPSILON
                ? def.heightOffset() : 0.0;
        double ampMultiplier = Math.abs(def.amplitudeMultiplier() - 1.0) > EPSILON
                ? def.amplitudeMultiplier() : 1.0;

        return new ColumnContext(
                targetHeight, valleyFactor, biomeId,
                caveModifier, heightOffset, ampMultiplier);
    }
}
