package com.geoforge.engine.config.biome;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable record containing ALL per-biome parameters for terrain generation.
 *
 * <p>Merges fields from {@link com.geoforge.engine.biome.BiomeTerrainConfig} and
 * {@link com.geoforge.engine.feature.tree.BiomeTreeConfig} into a single unified
 * definition, plus adds climate envelope fields for {@link ClimateResolver}.
 *
 * <p>Use {@link #defaults()} for neutral values, and {@link #merge(BiomeDefinition)}
 * to apply a shallow override (per-field) on top of base defaults.
 *
 * @param id                    biome identifier (e.g. {@code "plains"})
 * @param heightOffset          Y offset added to base terrain height (blocks). 0 = no offset.
 * @param amplitudeMultiplier   multiplier for continental height amplitude. 1.0 = no change.
 * @param surfaceBlock          surface block material ID (empty string = use fallback)
 * @param subSurfaceBlock       sub-surface block material ID (empty string = use fallback)
 * @param surfaceHardness       surface block hardness factor in [0, 1]. 0.5 = default.
 * @param caveAmplitudeModifier multiplier for cave carving amplitude. 1.0 = no change.
 * @param treeType              tree type identifier (empty string = use default)
 * @param treeDensity           tree density in [0, 1], or -1.0 to use global default
 * @param minTreeHeight         minimum tree height in blocks (0 = use global default)
 * @param maxTreeHeight         maximum tree height in blocks (0 = use global default)
 * @param surfaceDepth          depth of surface blocks below the top block (3 = default).
 *                              Controls how many layers of surfaceBlock/subSurfaceBlock are placed.
 * @param treeVariantModifiers  map of variant name to weight multiplier; empty = no overrides
 * @param vegetationDensity     probability in [0, 1] that a column gets vegetation
 * @param allowFloatingPlants   whether floating plants are permitted in this biome
 * @param tempMin               minimum temperature envelope value (inclusive)
 * @param tempMax               maximum temperature envelope value (inclusive)
 * @param humidityMin           minimum humidity envelope value (inclusive)
 * @param humidityMax           maximum humidity envelope value (inclusive)
 * @param continentalnessMin    minimum continentalness envelope value (inclusive)
 * @param continentalnessMax    maximum continentalness envelope value (inclusive)
 * @param priority              sorting priority when multiple biome envelopes overlap
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public record BiomeDefinition(
        String id,
        double heightOffset,
        double amplitudeMultiplier,
        String surfaceBlock,
        String subSurfaceBlock,
        double surfaceHardness,
        double caveAmplitudeModifier,
        String treeType,
        double treeDensity,
        int minTreeHeight,
        int maxTreeHeight,
        int surfaceDepth,
        Map<String, Double> treeVariantModifiers,
        List<String> vegetationTypes,
        double vegetationDensity,
        boolean allowFloatingPlants,
        double tempMin,
        double tempMax,
        double humidityMin,
        double humidityMax,
        double continentalnessMin,
        double continentalnessMax,
        int priority) {

    /**
     * Returns a default biome definition with neutral/identity values.
     *
     * @return a new {@code BiomeDefinition} with no modifications
     */
    public static BiomeDefinition defaults() {
        return new BiomeDefinition(
                "",          // id
                0.0,         // heightOffset
                1.0,         // amplitudeMultiplier
                "",          // surfaceBlock
                "",          // subSurfaceBlock
                0.5,         // surfaceHardness
                1.0,         // caveAmplitudeModifier
                "",          // treeType
                -1.0,        // treeDensity
                0,           // minTreeHeight
                0,           // maxTreeHeight
                3,           // surfaceDepth
                Map.of(),    // treeVariantModifiers
                List.of(),   // vegetationTypes
                0.3,         // vegetationDensity
                false,       // allowFloatingPlants
                Double.NaN, Double.NaN,   // tempMin, tempMax
                Double.NaN, Double.NaN,   // humidityMin, humidityMax
                Double.NaN, Double.NaN,   // continentalnessMin, continentalnessMax
                0);          // priority
    }

    /**
     * Returns a new BiomeDefinition with non-default fields from {@code override}
     * applied on top of this definition.
     *
     * <p>This is a shallow copy with per-field override. Empty strings mean "use
     * the receiver's value". Sentinel values ({@code -1.0} for treeDensity, {@code 0}
     * for min/max tree heights, empty maps/lists) also fall through to the receiver.
     *
     * @param override the overriding definition (must not be null)
     * @return a new merged BiomeDefinition
     */
    public BiomeDefinition merge(BiomeDefinition override) {
        if (override == null) {
            return this;
        }
        String mergedId = !override.id().isEmpty() ? override.id() : this.id();
        double mergedHeightOffset = Double.compare(override.heightOffset(), 0.0) != 0 ? override.heightOffset() : this.heightOffset();
        double mergedAmplitude = Math.abs(override.amplitudeMultiplier() - 1.0) > 1e-12 ? override.amplitudeMultiplier() : this.amplitudeMultiplier();
        // Never override a non-empty surface block with an empty one
        String mergedSurface = !override.surfaceBlock().isEmpty() ? override.surfaceBlock() : this.surfaceBlock();
        String mergedSubSurface = !override.subSurfaceBlock().isEmpty() ? override.subSurfaceBlock() : this.subSurfaceBlock();
        double mergedSurfaceHardness = Math.abs(override.surfaceHardness() - 0.5) > 1e-12 ? override.surfaceHardness() : this.surfaceHardness();
        double mergedCaveAmp = Math.abs(override.caveAmplitudeModifier() - 1.0) > 1e-12 ? override.caveAmplitudeModifier() : this.caveAmplitudeModifier();
        String mergedTreeType = !override.treeType().isEmpty() ? override.treeType() : this.treeType();
        double mergedTreeDensity = override.treeDensity() >= 0.0 ? override.treeDensity() : this.treeDensity();
        int mergedMinHeight = override.minTreeHeight() > 0 ? override.minTreeHeight() : this.minTreeHeight();
        int mergedMaxHeight = override.maxTreeHeight() > 0 ? override.maxTreeHeight() : this.maxTreeHeight();
        int mergedSurfaceDepth = override.surfaceDepth() > 0 ? override.surfaceDepth() : this.surfaceDepth();
        Map<String, Double> mergedVariantMods = !override.treeVariantModifiers().isEmpty()
                ? override.treeVariantModifiers() : this.treeVariantModifiers();
        List<String> mergedVeg = !override.vegetationTypes().isEmpty()
                ? override.vegetationTypes() : this.vegetationTypes();
        double mergedVegDensity = Math.abs(override.vegetationDensity() - 0.3) > 1e-12 ? override.vegetationDensity() : this.vegetationDensity();
        boolean mergedFloat = override.allowFloatingPlants() || this.allowFloatingPlants();
        double mergedTempMin = !Double.isNaN(override.tempMin()) ? override.tempMin() : this.tempMin();
        double mergedTempMax = !Double.isNaN(override.tempMax()) ? override.tempMax() : this.tempMax();
        double mergedHumidityMin = !Double.isNaN(override.humidityMin()) ? override.humidityMin() : this.humidityMin();
        double mergedHumidityMax = !Double.isNaN(override.humidityMax()) ? override.humidityMax() : this.humidityMax();
        double mergedContMin = !Double.isNaN(override.continentalnessMin()) ? override.continentalnessMin() : this.continentalnessMin();
        double mergedContMax = !Double.isNaN(override.continentalnessMax()) ? override.continentalnessMax() : this.continentalnessMax();
        int mergedPriority = override.priority() != 0 ? override.priority() : this.priority();

        return new BiomeDefinition(
                mergedId, mergedHeightOffset, mergedAmplitude,
                mergedSurface, mergedSubSurface, mergedSurfaceHardness,
                mergedCaveAmp, mergedTreeType, mergedTreeDensity,
                mergedMinHeight, mergedMaxHeight,
                mergedSurfaceDepth,
                Collections.unmodifiableMap(mergedVariantMods),
                Collections.unmodifiableList(mergedVeg),
                mergedVegDensity, mergedFloat,
                mergedTempMin, mergedTempMax,
                mergedHumidityMin, mergedHumidityMax,
                mergedContMin, mergedContMax,
                mergedPriority);
    }
}
