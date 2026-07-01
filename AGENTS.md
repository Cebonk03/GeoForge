# GeoForge Knowledge Base

**Generated:** 2026-07-01
**Commit:** HEAD
**Branch:** main

**Stack:** Java 21/25 + Gradle 9.6 + Paper API (1.21.x / 26.x)

## Structure

```
geoforge/
├── engine/        # Zero-Bukkit math engine (3D density, noise, geology, biomes)
├── api/           # Adapter interface + version utilities + FoliaDetector
├── adapters/
│   ├── v1_21_x/  # Paper 1.21.x adapter (Java 21)
│   └── v26_x/    # Paper 26.x adapter (Java 25)
└── plugin/        # ShadowJAR — wires engine + adapters into ChunkGenerator/BiomeProvider
```

## Module Stats (Verified)

| Module | Main Srcs | Tests | Java | Role |
|--------|-----------|-------|------|------|
| engine | 70 | 61 | 21 | 3D density engine, zero Bukkit |
| api | 5 | 3 | 21 | Adapter interface + AbstractPaperAdapter + ServerVersion + FoliaDetector |
| v1_21_x | 1 | 1 | 21 | Paper 1.21.x adapter (RegistryAccess-based) |
| v26_x | 1 | 1 | 25 | Paper 26.x adapter (constructor injection for testability) |
| plugin | 5 | 4 | 25 | Plugin + ShadowJAR + GeoForgePluginTest |
| **Total** | **82** | **70** | — | **~1,891 tests, 0 failures** |

## 3D Density Architecture

```
density(x,y,z) = heightFunc(x,z) - y + caveNoise(x,y,z) * amplitude
Positive = solid, Negative = air
```

- `getDensity()` — 4-stage pipeline: computeBaseDensity → applyCaveNoise → applyEnhancedCaves → applyRiverCarving
- `getSurfaceHeight()` — binary search O(log n) on density field
- Caves: EnhancedCaveSystem with CaveType enum (SPAGHETTI/CHEESE/NOODLE) + Y-envelope gating
- Rivers: RiverProfile enum (VSHAPED/CANYON/FLOODPLAIN) via RiverCarver interface
- Biomes: 3D continuous noise (temperature × humidity × continentalness) via ClimateResolver
- Multi-noise terrain: ridge/FBM/flat blended by continentalness + boundary noise warp
- Features: 9 TreeType enum values × 11 CanopyProfile impls × 6 TrunkProfile impls + VegetationPlacer
- Erosion: 2D hydraulic erosion on extracted heightmap
- Wow moments: ScenicFeatureDetector (EDGE_VISTA/HIDDEN_VALLEY/EMERGENCE) with intensity gating (≥0.5)

## Where To Look

| Task | Location |
|------|----------|
| 3D density engine | `engine/.../GeoForgeEngine.java` |
| Cave noise config | `engine/.../config/GeoForgeConfig.java` |
| Version adapter interface | `api/.../adapter/GeoForgeAdapter.java` |
| Version selection | `plugin/.../AdapterFactory.java` |
| Paper integration | `plugin/.../GeoForgeGenerator.java` |
| Biome assignment | `plugin/.../GeoForgeBiomeProvider.java` |
| Server version parsing | `api/.../version/ServerVersion.java` |
| Tree system registry | `engine/.../feature/tree/TreeRegistry.java` |
| Biome defaults | `engine/.../config/biome/GeoForgeBiomeDefaults.java` |
| Wow-moment detection | `engine/.../feature/ScenicFeatureDetector.java` |

## CODE MAP (Verified)

| Symbol | Kind | Module | Role |
|--------|------|--------|------|
| `GeoForgePlugin` | `JavaPlugin` | plugin | Entry point — adapter+engine init, getDefaultWorldGenerator |
| `AdapterFactory` | factory | plugin | Selects adapter by major version |
| `GeoForgeAdapter` | interface | api | mapBlock, mapBiome, scheduleTask, isFolia |
| `AbstractPaperAdapter` | abstract class | api | Shared base — injected lookup functions |
| `Paper1_21_xAdapter` | impl | v1_21_x | RegistryAccess biome lookup, Java 21 |
| `Paper26xAdapter` | impl | v26_x | Constructor injection, Java 25 |
| `VanillaFallbackAdapter` | impl | api | Degraded fallback (always STONE + plains) |
| `GeoForgeEngine` | core | engine | Density = heightFunc - y + caveNoise*ampl, binary search surface |
| `GeoForgeConfig` | record | engine | ~48 immutable terrain params |
| `ColumnContext` | record | engine | Per-column context (targetHeight, valleyFactor, biomeId, modifiers) |
| `DensityFunctionTree` | @FunctionalInterface | engine | sample(x,y,z)→double; composable tree |
| `NoiseSource` | @FunctionalInterface | engine | sample2D/sample3D — GradientNoise or FastNoiseLiteSource |
| `GradientNoise` | noise impl | engine | Perlin-style gradient noise |
| `FastNoiseLiteSource` | noise impl | engine | FastNoiseLite adapter |
| `DomainWarpedNoiseSource` | noise decorator | engine | NoiseSource domain-warping decorator |
| `FractalNoise` | noise impl | engine | Multi-octave fractal noise |
| `GeoForgeBiomeDefaults` | defaults | engine | Hardcoded BiomeDefinition map for 60+ vanilla biomes |
| `BiomeRegistry` | registry | engine | Thread-safe runtime registry |
| `ClimateResolver` | resolver | engine | Climate-based biome resolution |
| `TectonicPlateMapper` | geology | engine | 12 plates with Voronoi centres |
| `HydraulicErosion` | geology | engine | 2D droplet-based heightmap erosion |
| `RiverCarver` | @FunctionalInterface | engine | Pluggable River carving |
| `SimplexRiverCarver` | RiverCarver impl | engine | Simplex-based river valley carving |
| `CanyonRiverCarver` | RiverCarver impl | engine | Steep-walled canyon |
| `FloodplainRiverCarver` | RiverCarver impl | engine | Wide floodplain |
| `DomainWarpedRiverCarver` | RiverCarver decorator | engine | Domain-warping decorator |
| `EnhancedCaveSystem` | cave utility | engine | 3-type cave: SPAGHETTI/CHEESE/NOODLE |
| `CaveType` | enum | engine | SPAGHETTI/CHEESE/NOODLE with Y-envelope gating |
| `DomainWarpDensity` | DensityFunctionTree | engine | Domain-warping density decorator |
| `MultiNoiseHeightFunction` | DensityFunctionTree | engine | Ridge/FBM/flat height blending |
| `PlateContinentalness` | DensityFunctionTree | engine | Tectonic plate influence |
| `StructurePlateauModifier` | utility | engine | Terrain flattening with feathered border |
| `ServerVersion` | record | api | Regex-parsed major.minor.patch version |
| `FoliaDetector` | utility | api | Class.forName check for Folia |
| `TreeType` | enum | engine | 9 types: OAK/BIRCH/SPRUCE/JUNGLE/ACACIA/DARK_OAK/PALE_OAK/CHERRY/MANGROVE |
| `TreeRegistry` | registry | engine | Default biome→TreeType map |
| `TreePlacer` | placer | engine | 9-type + 11 canopy × 6 trunk variant tree placement |
| `CanopyProfile` | @FunctionalInterface | engine | 11 implementations (Round, Oval, Domed, Conical, etc.) |
| `TrunkProfile` | @FunctionalInterface | engine | 6 implementations (Straight, Bent, Leaning, Twisted, etc.) |
| `TreeVariant` | record | engine | Named variant = trunk + canopy + height + weight |
| `TreeVariantSelector` | selector | engine | Deterministic noise-based variant selection |
| `BiomeTerrainConfig` | record | engine | Per-biome terrain modifiers (still active) |
| `ScenicFeatureDetector` | feature | engine | Wow-moment detection with intensity scoring |
| `RiverProfile` | enum | engine | VSHAPED/CANYON/FLOODPLAIN |
| `CaveYEnvelope` | utility | engine | Gaussian Y-envelope cave distribution |
| `DensityGuard` | utility | engine | Density clamping helper |
| `ThreadLocalBuffers` | utility | engine | ThreadLocal reusable float/double arrays |
| `BlockSetter` | @FunctionalInterface | engine | Block placement callback interface |
| `ConfigMigrator` | utility | engine | Config version migration |
| `VegetationPlacer` | placer | engine | Surface vegetation placement |
| `GeoForgeBiomeProvider` | BiomeProvider | plugin | Delegates biome queries to engine |
| `GeoForgeReloadCommand` | CommandExecutor | plugin | Hot reload biome definitions |

## Conventions

- **Zero NMS/Spigot imports** — compile only against `io.papermc.paper:paper-api`
- **RegistryAccess for biomes** — never `Registry.BIOME` (removed in 26.x)
- **RegionScheduler** for task scheduling — works on Paper and Folia
- **ThreadLocal** for per-thread caches — no `static` mutable fields
- **AtomicInteger/AtomicLong** only for concurrency — no `synchronized`/`ReentrantLock`
- **26.1.2.build.+** for Paper 26.x API dependency
- **1.21.11-R0.1-SNAPSHOT** for Paper 1.21.x API
- **Constructor injection** for testability (Paper26xAdapter)
- **Binary search** for surface height — O(log n)
- **ColumnContext** pre-computed per column for density queries

## Death Pills

| # | Rule |
|---|------|
| DP-1 | Zero `net.minecraft.*` or `craftbukkit.*` |
| DP-4 | Zero `Class.forName()` except `FoliaDetector.java` |
| DP-5 | Zero `synchronized`/`ReentrantLock`/static mutable maps |
| DP-7 | Zero `Registry.BIOME`/`Biome.valueOf`/`Material.valueOf`/`getScheduler()` |
| DP-8 | Zero hardcoded Y bounds — use `chunkData.getMinHeight/MaxHeight` |
| DP-9 | Zero biome logic in `ChunkGenerator` — delegate to `BiomeProvider` |

## Anti-Patterns

- `shouldGenerateBedrock()` deprecated — bedrock handled in `generateNoise()`
- `StructurePlateauModifier` wired in `erodeColumn()` when `plateauSize > 0`
- `ScaledNoise` + `ScaledNoise2D` implement `DensityFunctionTree` but unused in production
- Dynamic version ranges removed — all pinned in `gradle/libs.versions.toml`

## Test Conventions (Verified)

- **Framework**: JUnit 5 (Jupiter) + AssertJ + MockBukkit 4.110.0 + Mockito 5.23.0 + ArchUnit 1.4.2 + JMH 1.37
- **Tagging**: @Tag("unit") (56 files), @Tag("integration") (5), @Tag("architecture"), @Tag("threading"), @Tag("smoke"), @Tag("validation")
- **DisplayName**: EVERY test class and method — 526 @DisplayName annotations across source
- **Method naming**: methodName_scenario_expectedBehavior
- **Organization**: One test class per production class; `*IntegrationTest` suffix
- **Mockito**: Always programmatic (`mock()`), no `@ExtendWith`/`@Mock`/`@MockitoSettings`
- **Dual assertion**: JUnit 5 assertEquals + AssertJ assertThat
- **Determinism**: Every noise/cave/feature method has a seed-driven determinism test
- **RecordingSetter**: Inner class pattern for canopy/trunk block placement verification
- **Snapshot tests**: SHA-256 golden checksums for heightmap/density regression
- **Parameterized**: @MethodSource for grid patterns + @ValueSource for simple int sets
- **Performance budget**: assertTimeout(Duration.ofMillis(...)) on hotspot operations
- **JaCoCo thresholds**: 60% line / 50% branch minimum across all modules
- **MockBukkit**: JDK 21 modules only; v26_x + plugin use Mockito-only, runtime tested on real Paper 26.x

## CI/CD

| Pipeline | Triggers | JDK | Key Steps |
|----------|----------|-----|-----------|
| **Fast CI** | push/PR to main | 21+25 | Validate → Compile split → Unit tests (<3 min) |
| **Full CI** | workflow_dispatch | 21+25 | Compile → Test → SpotBugs+Checkstyle+ArchUnit+banned-API-scan → JaCoCo → ShadowJAR → Paper runtime smoke test |
| **Release** | tag v* / dispatch | 21+25 | Test → ShadowJAR → checksums → GitHub Release |

```bash
# Local CI-equivalent (verified)
./gradlew :engine:test :api:test :adapters:v1_21_x:test  # JDK 21 modules
./gradlew :adapters:v26_x:test :plugin:test              # JDK 25 modules
./gradlew classes                                          # Compile all toolchains
./gradlew bannedApiScan                                    # Banned API regex scan
./gradlew :plugin:shadowJar                                # Build deployable artifact
```

**Banned API scan** enforces: `Registry.BIOME`, `Biome.valueOf/values()`, `Material.valueOf/getMaterial/matchMaterial`, `getScheduler().run`, `chunkData.setBiome()`, `net.minecraft.*`, `craftbukkit.*`, `ReentrantLock`, `synchronized`, `Class.forName` (except FoliaDetector.java)

**Dependabot**: `.github/dependabot.yml` — weekly, grouped by test-deps/gradle-plugins, `automerge` label

## AI Agent Protocol

### Ground-Truth First
- **Never use training data** for Paper API, Bukkit, Gradle, or library-specific knowledge.
  Use `context7_resolve-library-id` / `context7_query-docs` or `websearch`:
  - Paper API method signatures, registry keys, or behavior changes
  - Gradle DSL syntax, plugin configuration, or version catalog format
  - Any library version, API deprecation, or migration guide
- **Codebase questions**: use `codegraph_explore` — never guess file paths or symbol names.
- Cite external sources when answering API/library questions.


### CI/CD (reference)
See `.github/workflows/` for exact pipeline definitions.

### Banned API Scan (reference)
`build.gradle.kts` defines the `bannedApiScan` task with all banned patterns.

## Commit Messages

- `Feat:` / `Fix:` / `Chore:` / `Docs:` / `CI:` / `Test:` prefix with capital
- Message body: What changed and why
