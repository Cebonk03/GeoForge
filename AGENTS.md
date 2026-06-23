# GeoForge Knowledge Base

**Stack:** Java 21/25 + Gradle 9.6 + Paper API (1.21.x / 26.x)

## Structure

```
geoforge/
├── engine/        # Zero-Bukkit math engine (noise, density, geology, biomes)
├── api/           # Adapter interface + version utilities + FoliaDetector
├── adapters/
│   ├── v1_21_x/  # Paper 1.21.x adapter (Java 21)
│   └── v26_x/    # Paper 26.x adapter (Java 25)
└── plugin/        # ShadowJAR — wires engine + adapters into ChunkGenerator/BiomeProvider
```

## Module Stats

| Module | Main Srcs | Tests | Java | Role |
|--------|-----------|-------|------|------|
|| engine | 16 | 11 | 21 | Math engine, zero Bukkit |
| api | 4 | 1 | 21 | Adapter interface + ServerVersion |
| v1_21_x | 1 | 1 | 21 | Paper 1.21.x adapter |
| v26_x | 1 | 1 | 25 | Paper 26.x adapter |
| plugin | 4 | 3 | 25 | Plugin + ShadowJAR |
|| **Total** | **26** | **18** | — | **103 tests, 0 failures** |

## Where To Look

| Task | Location |
|------|----------|
| Terrain generation math | `engine/src/main/java/com/geoforge/engine/` |
| Version adapter interface | `api/src/main/java/com/geoforge/api/adapter/GeoForgeAdapter.java` |
| Version selection | `plugin/src/main/java/com/geoforge/plugin/AdapterFactory.java` |
| Paper integration | `plugin/src/main/java/com/geoforge/plugin/GeoForgeGenerator.java` |
| Biome assignment | `plugin/src/main/java/com/geoforge/plugin/GeoForgeBiomeProvider.java` |
| Server version parsing | `api/src/main/java/com/geoforge/api/version/ServerVersion.java` |

## Conventions

- **Zero NMS/Spigot imports** — compile only against `io.papermc.paper:paper-api`
- **RegistryAccess for biomes** — never `Registry.BIOME` (removed in 26.x)
- **RegionScheduler** for task scheduling — single code path works on Paper and Folia
- **ThreadLocal** for per-thread caches — no `static` mutable fields
- **`AtomicInteger`/`AtomicLong`** only for concurrency — no `synchronized`/`ReentrantLock`
- **`26.1.2.build.+`** for Paper 26.x API dependency (pinned to 26.1.2.x)
- **`1.21.11-R0.1-SNAPSHOT`** for Paper 1.21.x API (matches MockBukkit 4.110.0)

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

- No version catalog (`gradle/libs.versions.toml`) — versions hardcoded in 5 build files
- Dynamic `26.+` range in plugin's Paper API dep — non-reproducible builds **(FIXED: pinned to `26.1.2.build.+`)**
- Deprecation warnings from `shouldGenerateBedrock()` (Paper 26.x deprecated this method)
- `engine/AGENTS.md` structure manually updated — keep in sync with file changes

## Commands

```bash
./gradlew :engine:test :api:test :adapters:v1_21_x:test :adapters:v26_x:test :plugin:test
./gradlew :plugin:shadowJar
```
