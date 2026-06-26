# GeoForge Engine

Zero-Bukkit math engine for terrain generation. All classes pure Java 21 with no server dependencies.

## Structure

```
engine/src/main/java/com/geoforge/engine/
engine/src/main/java/com/geoforge/engine/
├── config/       GeoForgeConfig.java (49-field immutable record), ConfigMigrator.java
├── noise/        NoiseSource.java, SimplexNoise.java, FractalNoise.java,
│                FastNoiseLite.java, FastNoiseLiteSource.java
├── density/      DensityFunctionTree.java + 17 impls (Add, Clamp, CanyonRiverCarver,
│                CaveYEnvelope, Constant, DomainWarpDensity, EnhancedCaveSystem,
│                FloodplainRiverCarver, MultiNoiseHeightFunction, Multiply,
│                NoopRiverCarver, PlateContinentalness, RiverCarver, ScaledNoise,
│                ScaledNoise2D, SimplexRiverCarver)
├── geology/      TectonicPlateMapper.java, HydraulicErosion.java
├── biome/        BiomeLookupTable.java, BiomeTerrainConfig.java
├── plateau/      StructurePlateauModifier.java (terrain flattening, unwired)
├── feature/      BlockSetter.java, GeoForgeFeature.java, TreePlacer.java,
│                VegetationPlacer.java
├── util/         DensityGuard.java, ThreadLocalBuffers.java
└── GeoForgeEngine.java (3D density: heightFunc - y + caveNoise)
├── noise/        SimplexNoise.java, FractalNoise.java
├── density/      DensityFunctionTree.java + 9 impls (Constant, ScaledNoise, etc.)
├── geology/      TectonicPlateMapper.java, HydraulicErosion.java
├── biome/        BiomeLookupTable.java (8×8 temp×humidity grid)
├── plateau/      StructurePlateauModifier.java (terrain flattening, unwired)
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
| `config` | 2 | 2 | GeoForgeConfig (49 params), ConfigMigrator |
| `noise` | 5 | 3 | NoiseSource, SimplexNoise, FractalNoise, FastNoiseLite, FastNoiseLiteSource |
| `density` | 17 | 10 | DensityFunctionTree + 17 implementations incl. RiverCarvers, CaveSystem |
| `geology` | 2 | 2 | Tectonic plate mapper, hydraulic erosion simulation |
| `biome` | 2 | 1 | BiomeLookupTable, BiomeTerrainConfig |
| `plateau` | 1 | 1 | Terrain flattening utility (unwired in production) |
| `feature` | 4 | 2 | GeoForgeFeature, BlockSetter, TreePlacer, VegetationPlacer |
| `util` | 2 | 2 | DensityGuard, ThreadLocalBuffers |
| root | 1 | 5 | GeoForgeEngine + Density3D, Integration, Snapshot, ThreadSafety |
