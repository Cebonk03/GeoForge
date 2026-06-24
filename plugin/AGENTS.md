# GeoForge Plugin

Paper integration layer. ShadowJAR output — the deployable artifact.

## Where To Look

| Task | File |
|------|------|
| Plugin lifecycle | `plugin/src/main/java/com/geoforge/plugin/GeoForgePlugin.java` |
| Chunk generation logic | `plugin/src/main/java/com/geoforge/plugin/GeoForgeGenerator.java` |
| 3D density pipeline | `plugin/src/main/java/com/geoforge/plugin/GeoForgeGenerator.java#generateNoise()` |
| Biome provider | `plugin/src/main/java/com/geoforge/plugin/GeoForgeBiomeProvider.java` |
| Version adapter selection | `plugin/src/main/java/com/geoforge/plugin/AdapterFactory.java` |
| Plugin metadata | `plugin/src/main/resources/plugin.yml` |
| Seed configuration | `plugin/src/main/resources/config.yml` |

## Pass-Through Flags

```yaml
shouldGenerateNoise: false        # custom 3D density terrain
shouldGenerateSurface: false      # custom surface from density
shouldGenerateBedrock: false      # custom 5-layer bedrock in noise phase
shouldGenerateCaves: false        # 3D noise-based caves in density field
shouldGenerateDecorations: true   # vanilla (trees, ores, flowers)
shouldGenerateStructures: true    # vanilla (villages, strongholds)
shouldGenerateMobs: true          # vanilla
```

## Conventions

- `folia-supported: true` in `plugin.yml` — mandatory for Folia
- `api-version: '1.21.4'` — minimum supported (compiled against 1.21.11+)
- `load: STARTUP` — generator must be ready before worlds load
- AdapterFactory uses pure integer switch, no reflection
- AdapterFactory lives here (not `api`) because it needs visibility into all adapter impls

## Anti-Patterns

- No hardcoded `Material` constants — always go through `adapter.mapBlock()`
- No biome assignment in `GeoForgeGenerator` — delegate to `GeoForgeBiomeProvider`
- No `Bukkit.getScheduler()` — use `getRegionScheduler().execute(plugin, loc, run)`
