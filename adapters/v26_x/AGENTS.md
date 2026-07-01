# GeoForge v26_x Adapter

**Generated:** 2026-07-01
**Commit:** HEAD
**Branch:** main

Paper 26.x version adapter. Java 25, constructor injection for testability.

## Where To Look

| Task | File |
|------|------|
| Block/biome mapping | `src/main/java/.../Paper26xAdapter.java` |
| Adapter tests | `src/test/java/.../Paper26xAdapterTest.java` |

## Conventions

- Extends `AbstractPaperAdapter` (shared ~70% logic with v1_21_x adapter)
- Constructor injection: `Function<String, Material>` + `Function<String, Biome>` for testability
- Uses `RegistryAccess` for block and biome lookup

## Known Testing Gaps

- **Biome mapping (Paper 26.x)**: MockBukkit 4.110.0 lacks Paper 26.x biome support.
  Constructor injection enables isolated unit tests; integration tested in CI runtime smoke test.
