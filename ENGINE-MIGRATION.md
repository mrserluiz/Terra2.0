# Terra 2.0 engine migration

Terra 2.0 is a standalone Paper world-generation project derived from the Terra engine architecture. Its migration goal is to retain the useful Terra pack/addon ecosystem while introducing an independent generation core, strict per-world isolation and a controlled path for native Minecraft datapack resources.

## Architecture

| Layer | Location | Terra2 responsibility |
| --- | --- | --- |
| Terra compatibility API | `terra2-engine/common/api` | Preserve pack/addon contracts |
| Recovered Terra runtime | `terra2-engine/common/implementation` | Load registries, packs, configs and generation stages |
| Terra2 core | `terra2-engine/common/implementation/terra2-core` | Platform-independent plans, bindings, routing and composition |
| Built-in addons | `terra2-engine/common/addons` | Biomes, noise, palettes, features and structures for Terra packs |
| Paper adapter | `terra2-engine/platforms/bukkit` | Bind generation to Paper worlds/chunks/biomes |
| Authorization & manifests | Terra2 Paper integration | Protect worlds and persist generation identity |
| TerraPack converter | Terra2 conversion layer | Compile supported vanilla resources to isolated executable plans |

## Safety invariants

1. Generation is disabled after a fresh install.
2. A world must be explicitly authorized.
3. Primary vanilla worlds/dimensions are protected by default.
4. Existing worlds are not silently rebound to another generator.
5. Managed worlds persist generation identity/fingerprints.
6. Missing/incompatible packs and changed active sources fail closed.
7. Seed, dimension and height-bound changes are rejected for an active binding.
8. Existing chunks are not rewritten.
9. Vanilla datapack conversion does not globally install arbitrary resources into the server.

## Milestones reached by 7.0.14

### R0 — source recovery
Recovered the Terra API/runtime/addon architecture needed for Community Pack compatibility.

### R1 — modern Paper migration
Ported the recovered engine toward the modern Paper/Java runtime, including identifier/resource-key, biome/environment and NMS compatibility work.

### R2 — native Community Pack runtime
Community Packs load through the recovered Terra backend without requiring a separate legacy Terra plugin.

### R3 — safe world binding
Authorization, protected worlds, manifests and immutable generation bindings are integrated into the generation path.

### R4 — executable Terra2 core
`GenerationManager` routes generation through platform-independent plans. Terra Community Packs can be represented by compatibility plans while retaining their proven Terra generation backend.

### R5 — first vanilla importer
7.0.13 introduced the first executable vanilla datapack path for a deliberately narrow supported flat-world subset.

### R6 — TerraPacks and composition
7.0.14 adds immutable local TerraPacks, asynchronous conversion, `READY`/`BLOCKED` reporting and composition of one terrain base with supported additive features.

## Reference build: 7.0.14-BETA

The 7.0.14 reference build supports two distinct generation families:

- **Terra Community Packs:** executed through the recovered Terra compatibility backend.
- **Terra2/vanilla conversion:** supported resources are compiled into neutral Terra2 plans/TerraPacks.

These paths should not be conflated. Terra2 does not need to translate a Community Pack into vanilla datapack IR in order to run it.

## Next migration targets

The remaining migration is semantic rather than merely syntactic:

- richer vanilla noise/density/surface compilation;
- custom biome and dimension-type provisioning;
- world-isolated structure/template registries;
- jigsaw assembly and structure placement;
- processor execution;
- persistent structure starts/references;
- world-scoped loot execution;
- item provenance/NBT or component customization by world;
- deterministic save/restart integration tests;
- broader Community Pack regression testing.

Post-7.0.14 documents or source work may explore some of these areas. They are development milestones, not automatically capabilities of the 7.0.14 reference JAR.

## Pack-development direction

Terra2 is intended to support independent packs per world and eventually make pack authoring/recovery easier without coupling every pack to the main engine repository. This direction also enables a future Terra2Dev-style toolchain for validating, converting and developing packs separately from the server runtime.
