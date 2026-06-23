# GeoForge Engine

Zero-Bukkit math engine for terrain generation. All classes pure Java 21 with no server dependencies.

## Structure

```
engine/src/main/java/com/geoforge/engine/
├── noise/        SimplexNoise.java
├── density/      DensityFunctionTree.java + 5 impls (Constant, Scaled, Add, Clamp, Multiply)
├── geology/      TectonicPlateMapper.java, HydraulicErosion.java
├── biome/        BiomeLookupTable.java (8×8 temp×humidity grid)
├── plateau/      StructurePlateauModifier.java
└── GeoForgeEngine.java
```

## Where To Look

| Task | File |
|------|------|
| Add new noise type | `engine/src/main/java/com/geoforge/engine/noise/` |
| Compose terrain density | `engine/src/main/java/com/geoforge/engine/density/DensityFunctionTree.java` |
| Modify biome palette | `engine/src/main/java/com/geoforge/engine/biome/BiomeLookupTable.java` |
| Adjust erosion parameters | `engine/src/main/java/com/geoforge/engine/geology/HydraulicErosion.java` |

## Conventions

- Zero Bukkit/Paper imports enforced by ArchUnit (`EngineIsolationTest`)
- All noise seeded from `long` values — deterministic output
- All noise seeded from `long` values — deterministic output and ThreadLocal-cached

## Anti-Patterns

- No `java.util.Random` — use `SplittableRandom`
- No mutable shared state — use `ThreadLocal` for per-thread caches
