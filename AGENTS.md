# GeoForge Knowledge Base
**Generated:** 2026-06-30T02:15:00Z
**Commit:** 1e61eb8
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

## Module Stats

| Module | Main Srcs | Tests | Java | Role |
|--------|-----------|-------|------|------|
|| engine | 63 | 54 | 21 | 3D density engine, zero Bukkit |
| api | 5 | 3 | 21 | Adapter interface + AbstractPaperAdapter + ServerVersion + FoliaDetector |
| v1_21_x | 1 | 1 | 21 | Paper 1.21.x adapter |
| v26_x | 1 | 1 | 25 | Paper 26.x adapter (constructor injection for testability) |
| plugin | 4 | 4 | 25 | Plugin + ShadowJAR + GeoForgePluginTest |
||||| **Total** | **74** | **63** | — | **~2,126 tests, 0 failures** |

## 3D Density Architecture

```
density(x,y,z) = heightFunc(x,z) - y + caveNoise(x,y,z) * amplitude
Positive density = solid, negative density = air
```

- Engine: `GeoForgeEngine.getDensity()` + `getSurfaceHeight()` (binary search)
- Generator: `GeoForgeGenerator.generateNoise()` uses per-block density sampling
- Caves: Enhanced 3-type system with CaveType enum (SPAGHETTI/CHEESE/NOODLE) and Y-envelope gating
- Rivers: 3-profile system with RiverProfile enum (VSHAPED/CANYON/FLOODPLAIN) via RiverCarver interface
- Biomes: 3D continuous noise (temperature × humidity × continentalness)
- Multi-noise terrain: ridge/FBM/flat blended by continentalness + erosion
- Features: TreePlacer with 9-type tree system (OAK/BIRCH/SPRUCE/JUNGLE/ACACIA/DARK_OAK/PALE_OAK/CHERRY/MANGROVE) + 11 canopy profiles × 6 trunk profiles + vegetation placer in generateSurface()
- Erosion: 2D hydraulic erosion on extracted heightmap (for future 3D adaptation)

## Where To Look

| Task | Location |
|------|----------|
| 3D density engine | `engine/src/main/java/com/geoforge/engine/GeoForgeEngine.java` |
| Cave noise config | `engine/src/main/java/com/geoforge/engine/config/GeoForgeConfig.java` (caveFrequency, caveAmplitude, etc.) |
| Version adapter interface | `api/src/main/java/com/geoforge/api/adapter/GeoForgeAdapter.java` |
| Version selection | `plugin/src/main/java/com/geoforge/plugin/AdapterFactory.java` |
| Paper integration | `plugin/src/main/java/com/geoforge/plugin/GeoForgeGenerator.java` |
| Biome assignment | `plugin/src/main/java/com/geoforge/plugin/GeoForgeBiomeProvider.java` |
| Server version parsing | `api/src/main/java/com/geoforge/api/version/ServerVersion.java` |
|| Tree system registry | `engine/src/main/java/com/geoforge/engine/feature/tree/TreeRegistry.java` |

## CODE MAP

| Symbol | Kind | Module | Refs | Role |
|--------|------|--------|------|------|
| `GeoForgePlugin` | `JavaPlugin` | plugin | — | Entry point — adapter+engine init, getDefaultWorldGenerator |
| `AdapterFactory` | factory | plugin | 1 | Selects Paper1_21_xAdapter / Paper26xAdapter / VanillaFallbackAdapter by major version |
| `GeoForgeAdapter` | interface | api | 4 | mapBlock, mapBiome, scheduleTask, isFolia — version-bridging contract |
| `AbstractPaperAdapter` | abstract class | api | 2 | Shared base for Paper adapters — injected lookup functions for testability |
| `Paper1_21_xAdapter` | impl | v1_21_x | 1 | RegistryAccess-based biome lookup, Java 21 |
| `Paper26xAdapter` | impl | v26_x | 1 | Function-injected lookups for testability, Java 25 |
| `VanillaFallbackAdapter` | impl | api | 1 | Degraded fallback — always STONE + plains biome |
| `GeoForgeEngine` | core | engine | 3 | Density = heightFunc - y + caveNoise*ampl, surface via binary search |
| `GeoForgeConfig` | record | engine | 2 | 48 immutable terrain params |
| `DensityFunctionTree` | @FunctionalInterface | engine | 9 | sample(x,y,z)→double; composable tree (Add/Clamp/Constant/Multiply/PlateContinentalness) |
| `NoiseSource` | @FunctionalInterface | engine | 33 | sample2D/sample3D — SimplexNoise or FastNoiseLiteSource via config switch |
| `FastNoiseLiteSource` | noise | engine | 1 | FastNoiseLite adapter implementing NoiseSource interface |
| `BiomeConfigLoader` | loader | engine | 1 | YAML-driven biome definition loader (terrain, trees, vegetation) — replaces BiomeLookupTable |
| `BiomeRegistry` | registry | engine | 1 | Runtime registry of loaded config-driven biome definitions |
| `ClimateResolver` | resolver | engine | 1 | Climate-based biome resolution from temperature×humidity×continentalness |
| `TectonicPlateMapper` | geology | engine | 1 | 12 plates with Voronoi centres + coastline modulation |
| `HydraulicErosion` | geology | engine | 1 | 2D droplet-based heightmap erosion |
| `RiverCarver` | @FunctionalInterface | engine | 1 | Pluggable 3D river carving (current: SimplexRiverCarver) |
| `SimplexRiverCarver` | RiverCarver impl | engine | 1 | 2D simplex noise river valley carving with configurable frequency/depth/width |
| `StructurePlateauModifier` | util | engine | 0 | Terrain flattening with feathered border (wired in erodeColumn when plateauSize > 0) |
| `ServerVersion` | record | api | 2 | Regex-parsed major.minor.patch version |
| `FoliaDetector` | util | api | 2 | Class.forName("io.papermc.paper.threadedregions.RegionizedServer") |
| `TreeRegistry` | registry | engine | 1 | Default biome→TreeType map + config lookup via biomeTreeMap() |
| `TreePlacer` | placer | engine | 1 | 9-type tree placement in generateSurface() |
| `TreeType` | enum | engine | 1 | OAK/BIRCH/SPRUCE/JUNGLE/ACACIA/DARK_OAK/PALE_OAK/CHERRY/MANGROVE — maps to 11 canopies × 6 trunks |
| `CanopyProfile` | @FunctionalInterface | engine | 1 | 9 canopy shapes (Round/Oval/Domed/Conical/Layered/Spreading/FlatHat/Sparse/NoCanopy) — 11 implementations |
| `TrunkProfile` | @FunctionalInterface | engine | 1 | 6 trunk profiles (Straight/Bent/Leaning/Twisted/MultiStem/Fallen) — 6 implementations |
| `EnhancedCaveSystem` | cave | engine | 1 | 3-type cave system: SPAGHETTI/CHEESE/NOODLE with Y-envelope gating |
| `RiverProfile` | enum | engine | 1 | VSHAPED/CANYON/FLOODPLAIN river profiles via RiverCarver interface |

## Conventions

- **Zero NMS/Spigot imports** — compile only against `io.papermc.paper:paper-api`
- **RegistryAccess for biomes** — never `Registry.BIOME` (removed in 26.x)
- **RegionScheduler** for task scheduling — single code path works on Paper and Folia
- **ThreadLocal** for per-thread caches — no `static` mutable fields
- **`AtomicInteger`/`AtomicLong`** only for concurrency — no `synchronized`/`ReentrantLock`
- **`26.1.2.build.+`** for Paper 26.x API dependency (pinned to 26.1.2.x builds — Paper doesn't publish point releases for 26.x)
- **`1.21.11-R0.1-SNAPSHOT`** for Paper 1.21.x API (matches MockBukkit 4.110.0)
- **Constructor injection for testability** — Paper26xAdapter takes Function<String,Material> + Function<String,Biome> in pkg-private ctor
- **Binary search for surface height** — getSurfaceHeight() uses O(log n) binary search, not linear scan

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

- (all dynamic version ranges removed; versions pinned in `gradle/libs.versions.toml`)
- `26.1.2.build.+` is NOT a dynamic range — it resolves to the latest stable build of 26.1.2.x (Paper's versioning scheme)
- `shouldGenerateBedrock()` deprecated but we handle bedrock in `generateNoise()` now
- `StructurePlateauModifier` wired in `GeoForgeEngine.erodeColumn()` when `plateauSize > 0` for structure terrain flattening
- `ScaledNoise` + `ScaledNoise2D` implement `DensityFunctionTree` but unused in production — available for future density tree composition

## Test Conventions

- **Framework**: JUnit 5 (Jupiter 6.1.0) + AssertJ (3.27.7) + MockBukkit (4.110.0) + Mockito (5.23.0) + ArchUnit (1.4.2) + JMH (1.37)
- **Tagging**: @Tag("unit") (47 files), @Tag("integration") (7), @Tag("architecture"), @Tag("threading"), @Tag("smoke"), @Tag("validation")
- **DisplayName**: EVERY test class and method uses @DisplayName — 494 total across 62 test files
- **Method naming**: methodName_scenario_expectedBehavior (e.g. getDensity_atSurface_isNearZero)
- **Organization**: One test class per production class; *IntegrationTest suffix for pipeline tests
- **Mockito**: Always programmatic (mock()), no @ExtendWith/@Mock/@MockitoSettings
- **Dual assertion**: Both JUnit 5 (assertEquals/assertThrows) AND AssertJ (assertThat().isBetween())
- **Determinism**: Every noise/cave/feature method has a seed-driven determinism test
- **RecordingSetter**: Inner class pattern capturing block placement for canopy/trunk verification
- **Snapshot tests**: SHA-256 golden checksums for heightmap/density regression detection
- **Parameterized**: @MethodSource for grid patterns + @ValueSource for simple int sets
- **Performance budget**: assertTimeout(Duration.ofMillis(...)) on hotspot operations
- **JaCoCo thresholds**: 60% line / 50% branch minimum across all modules
- **MockBukkit**: JDK 21 modules only; v26_x + plugin use Mockito-only, runtime tested on real Paper 26.x

## AI Agent Protocol

### Ground-Truth First
- **Never use training data** for Paper API, Bukkit, Gradle, or library-specific knowledge.
  Always use `context7_resolve-library-id` / `context7_query-docs` or `websearch`:
  - Paper API method signatures, registry keys, or behavior changes
  - Gradle DSL syntax, plugin configuration, or version catalog format
  - Any library version, API deprecation, or migration guide
- **Codebase questions**: use `codegraph_explore` — never guess file paths or symbol names.
- Cite external sources when answering API/library questions.

### Gradle Workflow (Efficiency)
- **Targeted tasks**: `./gradlew :engine:compileJava` instead of full build when only engine changes.
- **Fast verification**: `./gradlew :module:compileJava -q` (daemon warm = faster than no-daemon).
- **Compile all**: `./gradlew classes` (all toolchains, main + test sources).
- **Single test**: `./gradlew :engine:test --tests "*.ClassName.methodName"` — no full suite.
- **Don't `clean`** unless build cache is stale — `build/` is already in `.gitignore`.
- **Daemon is good**: keep it running; use `--no-daemon` only in CI.
- **ArchUnit isolation test**: `./gradlew :engine:test --tests "com.geoforge.engine.arch.*"`
- **Build artifact**: `./gradlew :plugin:shadowJar` — output at `plugin/build/libs/GeoForge.jar`
- **JMH benchmarks**: `./gradlew :engine:jmh`

## CI/CD

| Pipeline | Triggers | JDK | Key Steps |
|----------|----------|-----|-----------|
| **Fast CI** | push/PR to main | 21+25 | Validate → Compile split → Unit tests (no SpotBugs/Checkstyle/coverage/ShadowJAR/runtime — <3 min) |
| **Full CI** | workflow_dispatch | 21+25 | Compile → Unit tests → SpotBugs+Checkstyle+ArchUnit+banned-API-scan → JaCoCo coverage → ShadowJAR → Paper runtime smoke test |
| **Release** | tag v* / dispatch | 21+25 | Test → ShadowJAR → checksums → GitHub Release with assets |

```bash
# Local CI-equivalent commands
./gradlew :engine:test :api:test :adapters:v1_21_x:test  # JDK 21 modules
./gradlew :adapters:v26_x:test :plugin:test              # JDK 25 modules
./gradlew classes                                          # Compile all toolchains
./gradlew :engine:spotbugsMain :api:spotbugsMain :adapters:v1_21_x:spotbugsMain :adapters:v26_x:spotbugsMain :plugin:spotbugsMain
./gradlew :engine:checkstyleMain :api:checkstyleMain :adapters:v1_21_x:checkstyleMain :adapters:v26_x:checkstyleMain :plugin:checkstyleMain
./gradlew bannedApiScan                                    # Banned API regex scan
./gradlew :engine:jmh                                      # JMH microbenchmarks
./gradlew :plugin:shadowJar                                # Build deployable artifact
```

**Banned API scan** (CI enforces): `Registry.BIOME`, `Biome.valueOf`, `Biome.values()`, `Material.valueOf`, `Material.getMaterial`, `Material.matchMaterial`, `Bukkit.getScheduler()`, `chunkData.setBiome()`, `net.minecraft.*`, `craftbukkit.*`, `ReentrantLock`, `synchronized`, `Class.forName` (except `FoliaDetector.java`)

**Dependabot**: configured in `.github/dependabot.yml` (weekly, grouped by test-deps/gradle-plugins, `automerge` label)


## Commit Messages

- `Feat:` / `Fix:` / `Chore:` / `Docs:` / `CI:` prefix with capital
- Message body: What changed and why, not boilerplate
