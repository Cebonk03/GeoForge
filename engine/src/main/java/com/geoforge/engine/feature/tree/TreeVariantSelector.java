package com.geoforge.engine.feature.tree;

import com.geoforge.engine.noise.NoiseSource;
import com.geoforge.engine.noise.GradientNoise;
import java.util.List;
import java.util.Map;

/**
 * Deterministic, noise-based selector for tree variants.
 *
 * <p>Given a list of weighted {@link TreeVariant}s for a species, and a block
 * position {@code (blockX, blockZ)}, this selector uses 2D SimplexNoise to
 * pick a variant deterministically. The same seed + position always yields the
 * same variant, and adjacent positions produce smoothly varying (spatially
 * coherent) variant choices.
 *
 * <p>Biome-specific variant weight modifiers can be applied via the optional
 * {@code biomeVariantModifiers} map (biomeId → variantName → weight multiplier).
 *
 * <p>This class is thread-safe after construction (immutable).
 */
public final class TreeVariantSelector {

    private static final double DEFAULT_FREQUENCY = 0.015;
    private static final long SEED_SALT = 0xDECAFCAFEL;

    private final NoiseSource noise;
    private final double frequency;
    private final Map<String, Map<String, Double>> biomeVariantModifiers;

    /**
     * Creates a selector with no biome-specific variant modifiers.
     *
     * @param seed the world generation seed (for deterministic selection)
     */
    public TreeVariantSelector(long seed) {
        this(seed, DEFAULT_FREQUENCY, Map.of());
    }

    /**
     * Creates a selector with the given noise frequency.
     *
     * @param seed      the world generation seed
     * @param frequency noise sampling frequency (lower = larger patches)
     */
    public TreeVariantSelector(long seed, double frequency) {
        this(seed, frequency, Map.of());
    }

    /**
     * Creates a selector with biome-specific variant weight modifiers.
     *
     * @param seed                    the world generation seed
     * @param frequency               noise sampling frequency
     * @param biomeVariantModifiers   map: biomeId → variantName → weight multiplier
     */
    public TreeVariantSelector(long seed, double frequency,
                               Map<String, Map<String, Double>> biomeVariantModifiers) {
        this.noise = new GradientNoise(seed ^ SEED_SALT);
        this.frequency = frequency > 0 ? frequency : DEFAULT_FREQUENCY;
        // Deep-copy for true immutability
        if (biomeVariantModifiers == null) {
            this.biomeVariantModifiers = Map.of();
        } else {
            var copy = new java.util.LinkedHashMap<String, Map<String, Double>>(biomeVariantModifiers.size());
            for (var entry : biomeVariantModifiers.entrySet()) {
                copy.put(entry.getKey(), Map.copyOf(entry.getValue()));
            }
            this.biomeVariantModifiers = Map.copyOf(copy);
        }
    }

    /**
     * Selects a variant from the given list, deterministically from position.
     *
     * @param variants the list of weighted variants to choose from
     * @param blockX   the world X-coordinate
     * @param blockZ   the world Z-coordinate
     * @return the selected variant, or {@code variants.get(0)} if the list has one entry
     * @throws IllegalArgumentException if {@code variants} is empty
     */
    public TreeVariant select(List<TreeVariant> variants, int blockX, int blockZ) {
        return select(variants, blockX, blockZ, null);
    }

    /**
     * Selects a variant with optional biome-specific weight modifiers.
     *
     * @param variants the list of weighted variants
     * @param blockX   the world X-coordinate
     * @param blockZ   the world Z-coordinate
     * @param biomeId  the biome ID for applying weight modifiers, or null
     * @return the selected variant
     * @throws IllegalArgumentException if {@code variants} is empty
     */
    public TreeVariant select(List<TreeVariant> variants, int blockX, int blockZ,
                              String biomeId) {
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("variant list must not be empty");
        }
        if (variants.size() == 1) {
            return variants.get(0);
        }

        Map<String, Double> modifiers = biomeId != null
                ? biomeVariantModifiers.getOrDefault(biomeId, Map.of())
                : Map.of();

        // Compute effective weights with modifiers
        double[] effectiveWeights = effectiveWeights(variants, modifiers);

        double totalWeight = 0.0;
        for (double w : effectiveWeights) {
            totalWeight += w;
        }

        if (totalWeight <= 0.0) {
            return variants.get(0);
        }

        // Map noise [-1, 1] → [0, 1] for weighted selection
        double noiseValue = noise.sample2D(blockX * frequency, blockZ * frequency);
        double target = (noiseValue + 1.0) / 2.0 * totalWeight;

        double cumulative = 0.0;
        for (int i = 0; i < variants.size(); i++) {
            cumulative += effectiveWeights[i];
            if (target <= cumulative) {
                return variants.get(i);
            }
        }
        return variants.getLast();
    }

    private static double[] effectiveWeights(List<TreeVariant> variants,
                                               Map<String, Double> modifiers) {
        double[] weights = new double[variants.size()];
        for (int i = 0; i < variants.size(); i++) {
            TreeVariant v = variants.get(i);
            double w = v.weight();
            Double mod = modifiers.get(v.name());
            if (mod != null) {
                w *= mod;
            }
            weights[i] = Math.max(0.0, w);
        }
        return weights;
    }
}
