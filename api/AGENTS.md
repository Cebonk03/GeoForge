# GeoForge API
**Generated:** 2026-07-01
**Commit:** HEAD
**Branch:** main

Adapter interface + version utilities bridging engine to Paper server.

## Structure

```
api/src/main/java/com/geoforge/api/
├── adapter/       GeoForgeAdapter.java (interface), AbstractPaperAdapter.java, VanillaFallbackAdapter.java
├── version/       ServerVersion.java (regex parser)
└── util/          FoliaDetector.java (sanctioned Class.forName)
```

## Where To Look

| Task | File |
|------|------|
| Add new adapter method | `api/.../adapter/GeoForgeAdapter.java` |
| Modify version parsing | `api/.../version/ServerVersion.java` |
| Folia detection logic | `api/.../util/FoliaDetector.java` |
| Degraded fallback | `api/.../adapter/VanillaFallbackAdapter.java` |

## Conventions

- `GeoForgeAdapter` implementations map engine block/biome IDs → Paper `Material`/`Biome`
- `ServerVersion` uses regex parse (`^(\\d+)\\.(\\d+)(?:\\.(\\d+))?$`) — never `startsWith("1.")`
- `FoliaDetector` is the **only** sanctioned `Class.forName()` in the entire codebase
- All biomes resolved via `RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME)` — never `Registry.BIOME`

## Anti-Patterns

- No `Biome.valueOf()` / `Biome.values()` / `Material.valueOf()`
- No `Bukkit.getScheduler().runTask()` — use `getRegionScheduler().execute(plugin, loc, run)`
