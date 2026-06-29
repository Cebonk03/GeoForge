package com.geoforge.engine.feature.tree;

import java.util.Collections;
import java.util.Map;

/**
 * Per-biome tree configuration overrides.
 *
 * <p>Each field provides an override for the global tree generation config.
 * Sentinel values ({@code -1.0} for density, {@code 0} for heights, empty map for modifiers)
 * mean "use the global / default value".
 *
 * @param treeDensity      tree density override [0,1], or {@code -1.0} to use global
 * @param minTreeHeight    minimum trunk height override (≥4), or {@code 0} to use global
 * @param maxTreeHeight    maximum trunk height override (≥4), or {@code 0} to use global
 * @param variantModifiers map of variant name to weight multiplier; empty = no overrides
 */
public record BiomeTreeConfig(
        double treeDensity,
        int minTreeHeight,
        int maxTreeHeight,
        Map<String, Double> variantModifiers) {

    /**
     * Returns a default configuration with all sentinel/neutral values.
     *
     * @return a new {@code BiomeTreeConfig} with no overrides
     */
    public static BiomeTreeConfig defaults() {
        return new BiomeTreeConfig(-1.0, 0, 0, Map.of());
    }

    public BiomeTreeConfig {
        if (treeDensity != -1.0 && (treeDensity < 0.0 || treeDensity > 1.0)) {
            throw new IllegalArgumentException(
                    "treeDensity must be in [0, 1] or -1.0 (sentinel), got " + treeDensity);
        }
        if (minTreeHeight < 0) {
            throw new IllegalArgumentException(
                    "minTreeHeight must be >= 0 (0 = use global), got " + minTreeHeight);
        }
        if (maxTreeHeight < 0) {
            throw new IllegalArgumentException(
                    "maxTreeHeight must be >= 0 (0 = use global), got " + maxTreeHeight);
        }
        if (minTreeHeight > 0 && maxTreeHeight > 0 && minTreeHeight > maxTreeHeight) {
            throw new IllegalArgumentException(
                    "minTreeHeight (" + minTreeHeight + ") > maxTreeHeight (" + maxTreeHeight + ")");
        }
        variantModifiers = variantModifiers != null
                ? Map.copyOf(variantModifiers)
                : Map.of();
    }
}
