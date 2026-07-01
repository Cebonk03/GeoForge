# GeoForge Engine
**Generated:** 2026-07-01
**Commit:** HEAD
**Branch:** main

Zero-Bukkit math engine for terrain generation. All classes pure Java 21 with no server dependencies.

## Structure

```
engine/src/main/java/com/geoforge/engine/
├── config/       GeoForgeConfig.java (48-field immutable record), ConfigMigrator.java, RiverProfile.java
├── noise/        NoiseSource.java, GradientNoise.java, FractalNoise.java, FastNoiseLite.java,
│                DomainWarpedNoiseSource.java, FastNoiseLiteSource.java
├── density/      DensityFunctionTree.java + 18 impls (Add, Clamp, CanyonRiverCarver,
│                CaveType, CaveYEnvelope, Constant, DomainWarpDensity, EnhancedCaveSystem,
│                FloodplainRiverCarver, MultiNoiseHeightFunction, Multiply,
│                NoopRiverCarver, PlateContinentalness, RiverCarver, ScaledNoise,
│                ScaledNoise2D, SimplexRiverCarver)
├── geology/      TectonicPlateMapper.java, HydraulicErosion.java
├── biome/        BiomeTerrainConfig.java
├── config/biome/  GeoForgeBiomeDefaults.java, BiomeDefinition.java, BiomeRegistry.java, ClimateResolver.java
├── plateau/      StructurePlateauModifier.java (terrain flattening, wired in erodeColumn)
├── feature/      BlockSetter.java, GeoForgeFeature.java, TreePlacer.java,
│                VegetationPlacer.java, tree/ (TreeType, TreeRegistry, CanopyProfile,
│                TrunkProfile, TreeVariant, TreeVariantSelector, BiomeTreeConfig,
│                TrunkResult + 11 canopy impls + 6 trunk impls), ScenicFeatureDetector.java
├── util/         DensityGuard.java, ThreadLocalBuffers.java
└── GeoForgeEngine.java (3D density: heightFunc - y + caveNoise)
```

## Where To Look

| Task | File |
|------|------|
| Add new noise type | `engine/.../noise/` |
| Compose terrain density | `engine/.../density/DensityFunctionTree.java` |
| Modify biome defaults | `engine/.../config/biome/GeoForgeBiomeDefaults.java` |
| 3D density / cave system | `engine/.../GeoForgeEngine.java#getDensity()` |
| River carving interface | `engine/.../density/RiverCarver.java` |
| Add tree type / canopy / trunk | `engine/.../feature/tree/` |

## Conventions

- Zero Bukkit/Paper imports enforced by ArchUnit (`EngineIsolationTest`)
- All noise seeded from `long` values — deterministic output
- Density: positive = solid, negative = air
- `RiverCarver` uses `@FunctionalInterface` for pluggable carving
- `DensityFunctionTree` implementations compose via Add/Multiply/Clamp/Constant — immutable, no mutation
- Surface height via binary search — `getSurfaceHeight()` is O(log n), not linear scan
- Seed decorrelation: `seed ^ 0xCONSTANT` per noise layer (GradientNoise/FractalNoise)

## Anti-Patterns

- No `java.util.Random` — use `SplittableRandom`
- No mutable shared state — use `ThreadLocal` for per-thread caches

## Module Stats

| Package | Source | Tests | Role |
|---------|--------|-------|------|
| `config` | 3 | 2 | GeoForgeConfig (48 params), ConfigMigrator, RiverProfile |
| `noise` | 6 | 4 | NoiseSource, GradientNoise, FractalNoise, FastNoiseLite, FastNoiseLiteSource, DomainWarpedNoiseSource |
| `density` | 19 | 12 | DensityFunctionTree + 18 impls |
| `geology` | 2 | 2 | TectonicPlateMapper, HydraulicErosion |
| `biome` | 1 | 1 | BiomeTerrainConfig |
| `config/biome` | 4 | 4 | BiomeDefinition, BiomeRegistry, ClimateResolver, GeoForgeBiomeDefaults |
| `feature` | 30 | 24 | GeoForgeFeature, BlockSetter, TreePlacer, VegetationPlacer, ScenicFeatureDetector + tree/ + canopy/ + trunk/ |
| `util` | 2 | 2 | DensityGuard, ThreadLocalBuffers |
| root | 2 | 6 | GeoForgeEngine, ColumnContext + Density3D, Integration, Snapshot, ThreadSafety |
