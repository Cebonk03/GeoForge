# GeoForge v1_21_x Adapter
**Generated:** 2026-07-01
**Commit:** HEAD
**Branch:** main

Paper 1.21.x version adapter. Java 21, RegistryAccess-based biome lookup.

## Where To Look

| Task | File |
|------|------|
| Block/biome mapping | `src/main/java/.../Paper1_21_xAdapter.java` |
| Adapter tests | `src/test/java/.../Paper1_21_xAdapterTest.java` |

## Conventions

- Extends `AbstractPaperAdapter` (shared ~70% logic with v26_x adapter)
- Uses `RegistryAccess` + `Registry.MATERIAL` for block and biome lookup
- MockBukkit 4.110.0 for integration tests
- Mockito for isolated unit tests
