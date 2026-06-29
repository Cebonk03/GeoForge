package com.geoforge.engine.feature.tree;

import java.util.Objects;

/**
 * A specific tree shape variant combining a trunk profile and canopy profile.
 *
 * <p>Each TreeVariant defines a distinct tree appearance: how the trunk grows,
 * how the leaves arrange, the height range, leaf density, and selection weight.
 * Variants are selected deterministically from position using
 * {@link TreeVariantSelector}.
 *
 * @param name         unique variant name (e.g. "tall_forest_oak", "plains_spreading")
 * @param trunk        the trunk profile defining trunk growth pattern
 * @param canopy       the canopy profile defining leaf arrangement
 * @param minHeight    minimum trunk height in blocks (must be &ge; 3)
 * @param maxHeight    maximum trunk height in blocks (must be &ge; minHeight)
 * @param leafDensity  leaf block density factor in [0, 1]; 1 = fully filled
 * @param weight       relative selection weight (&ge; 0); higher = more likely
 */
public record TreeVariant(
        String name,
        TrunkProfile trunk,
        CanopyProfile canopy,
        int minHeight,
        int maxHeight,
        double leafDensity,
        double weight) {

    public TreeVariant {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(trunk, "trunk must not be null");
        Objects.requireNonNull(canopy, "canopy must not be null");
        if (minHeight < 3) {
            throw new IllegalArgumentException(
                    "minHeight must be >= 3, got " + minHeight);
        }
        if (maxHeight < minHeight) {
            throw new IllegalArgumentException(
                    "maxHeight (" + maxHeight + ") < minHeight (" + minHeight + ")");
        }
        if (leafDensity < 0.0 || leafDensity > 1.0) {
            throw new IllegalArgumentException(
                    "leafDensity must be in [0, 1], got " + leafDensity);
        }
        if (weight < 0.0) {
            throw new IllegalArgumentException(
                    "weight must be >= 0, got " + weight);
        }
    }

    /**
     * Creates a copy of this variant with a different weight.
     * Useful for biome-specific weight overrides.
     *
     * @param newWeight the new weight value
     * @return a new TreeVariant with the updated weight
     */
    public TreeVariant withWeight(double newWeight) {
        return new TreeVariant(name, trunk, canopy, minHeight, maxHeight, leafDensity, newWeight);
    }
}
