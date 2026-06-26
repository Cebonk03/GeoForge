# GeoForge Engine

Zero-Bukkit math engine for terrain generation. All classes pure Java 21 with no server dependencies.

## Structure

```
engine/src/main/java/com/geoforge/engine/
├── config/       GeoForgeConfig.java (49-field immutable record with river params)
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
| `config` | 1 | 1 | Immutable terrain configuration (49 params) |
| `noise` | 2 | 2 | SimplexNoise + FractalNoise (multi-octave) |
| `density` | 11 | 4 | DensityFunctionTree interface + 9 implementations + RiverCarver |
| `geology` | 2 | 2 | Tectonic plate mapper, hydraulic erosion simulation |
| `biome` | 1 | 1 | 8×8 temperature-humidity biome lookup (38 vanilla IDs) |
| `plateau` | 1 | 1 | Terrain flattening utility (unwired in production) |
| root | 1 | 3 | GeoForgeEngine — orchestrates all subsystems |
