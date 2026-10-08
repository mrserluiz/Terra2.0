# Terra 2.0

Terra 2.0 is an independent continuation and modernization of the Terra world-generation engine for Paper, focused on **per-world generation**, **Community Pack compatibility**, safe world isolation, and a path toward native Minecraft datapack support.

> **Current reference build:** `Terra2-bukkit-7.0.14-BETA.jar`

Terra 2.0 is no longer only a recovery scaffold. The 7.0.14 line has an executable generation core, a Terra Community Pack compatibility backend, a first vanilla datapack importer, local TerraPacks, and per-world pack composition.

## What works in 7.0.14

- Terra Community Packs can be selected independently per authorized world.
- Existing Terra generation remains available through the compatibility backend.
- Generation is routed through the standalone Terra2 core and immutable world bindings.
- World authorization is explicit and generation is disabled by default.
- The primary vanilla worlds remain protected.
- A first vanilla datapack importer compiles the supported `minecraft:flat` subset.
- Local `.terrapack` archives distinguish `READY` from `BLOCKED` conversions.
- A world can compose one terrain base with supported additive feature packs.
- Conversion is asynchronous and preserves source/provenance data.
- Generation manifests/fingerprints protect worlds from silent pack/source changes.
- Diagnostic capture is available through `/terra2reportlog`.

## Community Packs

Known Terra-style packs remain the primary compatibility path. A world may select a single legacy pack directly:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]

worlds:
  terra2_teste:
    pack: OVERWORLD
```

Create it with Multiverse using the configured pack ID:

```text
/mv create terra2_teste normal -g Terra2
```

Exact generator syntax may depend on the world-management plugin and the selected Terra2 configuration path. Existing worlds should not be rebound to a different generation plan.

## TerraPacks and composition

7.0.14 introduces an immutable local TerraPack format and composition model. One terrain base can be combined with supported additive features:

```yaml
worlds:
  terra2_mix_teste:
    packs: [OVERWORLD, Debris]
```

For explicit composition, use:

```text
/mv create terra2_mix_teste normal --generator Terra2:PACKS
```

Conversion commands:

```text
/terra2 convert <ID> <input.zip> [namespace:dimension]
/terra2 convert <ID> <input-a.zip;input-b.zip> [namespace:dimension]
/terra2 packs list
/terra2 packs inspect <ID>
/terra2 reload
```

The 7.0.14 converter is intentionally strict. Unsupported semantic resources produce a `BLOCKED` TerraPack instead of being silently ignored.

See [TERRAPACKS.md](terra2-engine/TERRAPACKS.md) for the exact supported subset and test procedure.

## Safety model

Terra2 does not attach generation globally. A target world must be explicitly authorized and bound to a generation plan. The server primary world and vanilla dimensions are protected by default. Active bindings reject changes to source, pack selection, dimension, seed, or height bounds.

Only new chunks are generated. Terra2 does not rewrite existing chunks.

## Project layout

- `terra2-engine/` — recovered Terra engine plus the standalone Terra2 generation core and Paper integration.
- `terra2-engine/common/implementation/terra2-core/` — platform-independent generation routing and composition contracts.
- `terra2-engine/TERRAPACKS.md` — TerraPack conversion/composition reference.
- `terra2-engine/CORE-ARCHITECTURE.md` — executable core architecture and version milestones.
- `terra2-engine/DIAGNOSTICS.md` — crash/stall/console capture diagnostics.
- `ENGINE-MIGRATION.md` — migration history, safety rules and roadmap.
- `BUILD-STATUS.md` — current reference-build status and limitations.

## Current limitations

7.0.14 is **not a universal vanilla datapack converter**. Complex noise/density/surface graphs, custom biome/dimension types, jigsaw/template execution, processors, world-scoped loot and other advanced resources are not complete in this reference build.

Terra Community Packs and vanilla datapack conversion are separate paths: Community Packs use the compatibility backend; supported vanilla resources compile into Terra2's neutral generation model.

Development notes for post-7.0.14 structure/NBT/loot work may already exist in the repository, but they must not be interpreted as capabilities of the 7.0.14 reference JAR until integrated and server-validated.

## Development direction

The long-term goal is to make Terra2 a world-generation platform where each world can independently define terrain, biomes, structures, features and eventually world-scoped loot/item behavior, while retaining compatibility with the Terra pack ecosystem.

Terra2 is a successor project and is not an official release of the original Terra project.
