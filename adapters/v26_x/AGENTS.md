# GeoForge v26_x Adapter
**Generated:** 2026-06-30T02:15:00Z
**Commit:** 1e61eb8
**Branch:** main

Paper 26.x version adapter. Java 25, constructor injection for testability.

## Where To Look

| Task | File |
|------|------|
| Block/biome mapping | `src/main/java/com/geoforge/adapters/v26_x/Paper26xAdapter.java` |
| Adapter tests | `src/test/java/com/geoforge/adapters/v26_x/Paper26xAdapterTest.java` |

## Conventions

- Extends `AbstractPaperAdapter` (shared ~70% logic with v1_21_x adapter)
- Constructor injection: `Function<String, Material>` + `Function<String, Biome>` for testability
- Uses `RegistryAccess` for block and biome lookup

## Known Testing Gaps

- **Biome mapping (Paper 26.x)**: MockBukkit 4.110.0 does not support Paper 26.x biomes.
  The `Paper26xAdapter` uses constructor injection (`Function<String, Biome>`) for testability,
  but biome lookup integration tests require a live Paper 26.x server. The CI runtime-test
  step (ci-full.yml) exercises this against a real Paper 26.x server.
