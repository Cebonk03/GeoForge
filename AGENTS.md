# GeoForge Knowledge Base

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
| engine | 19 | 15 | 21 | 3D density engine, zero Bukkit |
| api | 4 | 2 | 21 | Adapter interface + ServerVersion + FoliaDetectorTest |
| v1_21_x | 1 | 1 | 21 | Paper 1.21.x adapter |
|| v26_x | 1 | 1 | 25 | Paper 26.x adapter (constructor injection for testability) |
| plugin | 4 | 4 | 25 | Plugin + ShadowJAR + GeoForgePluginTest |
||| **Total** | **29** | **23** | — | **~159 tests, 0 failures** |

## 3D Density Architecture

```
density(x,y,z) = heightFunc(x,z) - y + caveNoise(x,y,z) * amplitude
Positive density = solid, negative density = air
```

- Engine: `GeoForgeEngine.getDensity()` + `getSurfaceHeight()` (binary search)
- Generator: `GeoForgeGenerator.generateNoise()` uses per-block density sampling
- Caves: 3D SimplexNoise with configurable frequency/amplitude/octaves
- Rivers: `RiverCarver` interface (default: SimplexRiverCarver)
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

## CODE MAP

| Symbol | Kind | Module | Refs | Role |
|--------|------|--------|------|------|
| `GeoForgePlugin` | `JavaPlugin` | plugin | — | Entry point — adapter+engine init, getDefaultWorldGenerator |
| `AdapterFactory` | factory | plugin | 1 | Selects Paper1_21_xAdapter / Paper26xAdapter / VanillaFallbackAdapter by major version |
| `GeoForgeAdapter` | interface | api | 4 | mapBlock, mapBiome, scheduleTask, isFolia — version-bridging contract |
| `Paper1_21_xAdapter` | impl | v1_21_x | 1 | RegistryAccess-based biome lookup, Java 21 |
| `Paper26xAdapter` | impl | v26_x | 1 | Function-injected lookups for testability, Java 25 |
| `VanillaFallbackAdapter` | impl | api | 1 | Degraded fallback — always STONE + plains biome |
| `GeoForgeEngine` | core | engine | 3 | Density = heightFunc - y + caveNoise*ampl, surface via binary search |
|| `GeoForgeConfig` | record | engine | 2 | 22 immutable terrain params (with river params) |
| `DensityFunctionTree` | @FunctionalInterface | engine | 9 | sample(x,y,z)→double; composable tree (Add/Clamp/Constant/Multiply/PlateContinentalness) |
| `SimplexNoise` | noise | engine | 5 | 2D/3D simplex noise, deterministic from long seed |
| `FractalNoise` | noise | engine | 2 | Multi-octave fractal noise (sum octaves with lacunarity/persistence) |
| `BiomeLookupTable` | lookup | engine | 2 | 8x8 temp×humidity grid → 38 biome IDs |
| `TectonicPlateMapper` | geology | engine | 1 | 12 plates with Voronoi centres + coastline modulation |
| `HydraulicErosion` | geology | engine | 1 | 2D droplet-based heightmap erosion |
|| `RiverCarver` | @FunctionalInterface | engine | 1 | Pluggable 3D river carving (current: SimplexRiverCarver) |
|| `SimplexRiverCarver` | RiverCarver impl | engine | 1 | 2D simplex noise river valley carving with configurable frequency/depth/width |
| `StructurePlateauModifier` | util | engine | 0 | Terrain flattening with feathered border (unwired) |
| `ServerVersion` | record | api | 2 | Regex-parsed major.minor.patch version |
| `FoliaDetector` | util | api | 2 | Class.forName("io.papermc.paper.threadedregions.RegionizedServer") |

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
- `StructurePlateauModifier` exists but unwired — terrain flattening for future structure integration
- `ScaledNoise` + `ScaledNoise2D` implement `DensityFunctionTree` but unused in production — available for future density tree composition

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
./gradlew :plugin:shadowJar                                # Build deployable artifact
```

**Banned API scan** (CI enforces): `Registry.BIOME`, `Biome.valueOf`, `Material.valueOf`, `Bukkit.getScheduler()`, `chunkData.setBiome()`, `net.minecraft.*`, `craftbukkit.*`

**Dependabot**: configured in `.github/dependabot.yml`

## Commands

```bash
./gradlew :engine:test :api:test :adapters:v1_21_x:test :adapters:v26_x:test :plugin:test
./gradlew :plugin:shadowJar
```

## Commit Messages

- `Feat:` / `Fix:` / `Chore:` / `Docs:` / `CI:` prefix with capital
- Message body: What changed and why, not boilerplate
