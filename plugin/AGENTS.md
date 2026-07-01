# GeoForge Plugin
**Generated:** 2026-07-01
**Commit:** HEAD
**Branch:** main

Paper integration layer. ShadowJAR output — the deployable artifact.

## Structure

```
plugin/src/main/java/com/geoforge/plugin/
├── GeoForgePlugin.java        Main plugin class — onEnable, getDefaultWorldGenerator
├── GeoForgeGenerator.java     ChunkGenerator implementation (generateNoise, generateSurface)
├── GeoForgeBiomeProvider.java BiomeProvider — delegates to engine climate resolver
├── AdapterFactory.java        Selects adapter by major version (integer switch, no reflection)
├── command/
│   └── GeoForgeReloadCommand.java  Hot-reload biome definitions
└── resources/
    ├── paper-plugin.yml        Plugin metadata (folia-supported: true)
    └── config.yml              Terrain parameters
```

## Pass-Through Flags

```yaml
shouldGenerateNoise: false        # custom 3D density terrain
shouldGenerateSurface: false      # custom surface from density
shouldGenerateBedrock: false      # custom 5-layer bedrock in noise phase
shouldGenerateCaves: false        # 3D noise-based caves in density field
shouldGenerateDecorations: false  # GeoForge handles features in generateSurface()
shouldGenerateStructures: true    # vanilla (villages, strongholds)
shouldGenerateMobs: true          # vanilla
```

## Conventions

- `folia-supported: true` in `paper-plugin.yml` — mandatory for Folia
- `api-version: '1.21.4'` — minimum supported (compiled against 1.21.11+)
- `load: STARTUP` — generator must be ready before worlds load
- AdapterFactory uses pure integer switch, no reflection
- AdapterFactory lives here (not `api`) because it needs visibility into all adapter impls

## Anti-Patterns

- No hardcoded `Material` constants — always go through `adapter.mapBlock()`
- No biome assignment in `GeoForgeGenerator` — delegate to `GeoForgeBiomeProvider`
- No `Bukkit.getScheduler()` — use `getRegionScheduler().execute(plugin, loc, run)`
