# GeoForge Engine
**Generated:** 2026-06-28T16:01:49Z
**Commit:** 38deaaf
**Branch:** main

Zero-Bukkit math engine for terrain generation. All classes pure Java 21 with no server dependencies.

## Structure

```
engine/src/main/java/com/geoforge/engine/
├── config/       GeoForgeConfig.java (48-field immutable record), ConfigMigrator.java, RiverProfile.java
├── noise/        NoiseSource.java, SimplexNoise.java, FractalNoise.java,
│                FastNoiseLite.java, FastNoiseLiteSource.java
├── density/      DensityFunctionTree.java + 18 impls (Add, Clamp, CanyonRiverCarver,
│                CaveType, CaveYEnvelope, Constant, DomainWarpDensity, EnhancedCaveSystem,
│                FloodplainRiverCarver, MultiNoiseHeightFunction, Multiply,
│                NoopRiverCarver, PlateContinentalness, RiverCarver, ScaledNoise,
│                ScaledNoise2D, SimplexRiverCarver)
├── geology/      TectonicPlateMapper.java, HydraulicErosion.java
├── biome/        BiomeLookupTable.java, BiomeTerrainConfig.java
├── plateau/      StructurePlateauModifier.java (terrain flattening, wired in erodeColumn when plateauSize > 0)
├── feature/      BlockSetter.java, GeoForgeFeature.java, TreePlacer.java,
│                VegetationPlacer.java
├── util/         DensityGuard.java, ThreadLocalBuffers.java
└── GeoForgeEngine.java (3D density: heightFunc - y + caveNoise)

```

## Where To Look

| Task | File |
|------|------|
| Add new noise type | `engine/src/main/java/com/geoforge/engine/noise/` |
| Compose terrain density | `engine/src/main/java/com/geoforge/engine/density/DensityFunctionTree.java` |
| Modify biome palette | `engine/src/main/java/com/geoforge/engine/biome/BiomeLookupTable.java` |
| 3D density / cave system | `engine/src/main/java/com/geoforge/engine/GeoForgeEngine.java#getDensity()` |
| River carving interface | `engine/src/main/java/com/geoforge/engine/density/RiverCarver.java` |
| Adjust erosion parameters | `engine/src/main/java/com/geoforge/engine/geology/HydraulicErosion.java` |

## Conventions

- Zero Bukkit/Paper imports enforced by ArchUnit (`EngineIsolationTest`)
- All noise seeded from `long` values — deterministic output
- Density: positive = solid, negative = air
- `RiverCarver` uses `@FunctionalInterface` for pluggable carving
- `DensityFunctionTree` implementations compose via Add/Multiply/Clamp/Constant — immutable, no mutation
- Surface height via binary search — `getSurfaceHeight()` is O(log n), not linear scan
- Seed decorrelation: `seed ^ 0xCONSTANT` per noise layer (SimplexNoise/FractalNoise)

## Anti-Patterns

- No `java.util.Random` — use `SplittableRandom`
- No mutable shared state — use `ThreadLocal` for per-thread caches

## Commit Messages

- Format: `Feat:`, `Fix:`, `Chore:`, `Docs:` prefix with capital first letter
- Message body explains the what/why, not boilerplate

## Module Stats

| Package | Source | Tests | Role |
|---------|--------|-------|------|
| `arch` | 0 | 1 | ArchUnit — zero Bukkit dependency enforcement |
| `config` | 3 | 2 | GeoForgeConfig (48 params), ConfigMigrator, RiverProfile |
| `noise` | 5 | 3 | NoiseSource, SimplexNoise, FractalNoise, FastNoiseLite, FastNoiseLiteSource |
| `density` | 18 | 10 | DensityFunctionTree + 18 implementations incl. CaveType, RiverCarvers, CaveSystem |
| `geology` | 2 | 2 | Tectonic plate mapper, hydraulic erosion simulation |
| `biome` | 2 | 2 | BiomeLookupTable, BiomeTerrainConfig |
| `plateau` | 1 | 1 | Terrain flattening utility (wired in erodeColumn when plateauSize > 0) |
| `feature` | 4 | 2 | GeoForgeFeature, BlockSetter, TreePlacer, VegetationPlacer |
| `util` | 2 | 2 | DensityGuard, ThreadLocalBuffers |
| root | 1 | 5 | GeoForgeEngine + Density3D, Integration, Snapshot, ThreadSafety |


## Available But Unused

- **ScaledNoise** / **ScaledNoise2D** (`density/ScaledNoise.java`, `density/ScaledNoise2D.java`):
  DensityFunctionTree implementations available for future biome-specific noise scaling.
  ScaledNoise applies per-axis scaling on (x, y, z) inputs; ScaledNoise2D scales only (x, z).
  These can be wired into GeoForgeEngine's density tree for specialized terrain features
  (e.g., amplified mountains, compressed valleys).