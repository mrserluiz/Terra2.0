# Terra2 build status

## Current reference build

**Terra2-bukkit-7.0.14-BETA.jar**

This document describes the capabilities of the current reference JAR. Source files and development notes in the repository may contain work targeting later experimental milestones; those later notes are not automatically part of 7.0.14.

## Operational state

7.0.14 is beyond the original recovery scaffold. It contains an executable standalone generation core and can route authorized worlds through either:

1. the recovered Terra Community Pack compatibility backend; or
2. the supported neutral Terra2 plan/TerraPack path.

Generation remains **disabled by default**. No world is automatically authorized and the primary vanilla worlds remain protected.

## Implemented by 7.0.14

- Recovered Terra API, addon system, Community Pack loader and Paper generation backend.
- Java 25 / modern Paper migration work for the recovered engine.
- Explicit per-world authorization through Terra2 settings.
- Immutable generation bindings keyed to world, dimension, seed and height bounds.
- Core routing through `GenerationManager`.
- Terra compatibility plans that preserve the existing noise generator and biome provider.
- Native terrain, column sampling and biome-query routing through the core facade.
- Compatibility bridge for legacy Terra noise capabilities used by decoration.
- First executable vanilla datapack importer for the supported flat-world subset.
- Local immutable `.terrapack` conversion output.
- `READY` versus `BLOCKED` semantic conversion status.
- Asynchronous conversion workspace with provenance/source preservation.
- Composition of one terrain base with supported additive simple-block feature stages.
- Per-world generation manifests/fingerprints.
- Diagnostics for generation exceptions, stalls and console capture.
- `/terra2 reload` for authorization/settings changes.
- `/terra2reportlog` diagnostic capture.

## TerraPack workspace

The plugin uses:

```text
plugins/Terra2/conversion/input/
plugins/Terra2/conversion/reports/
plugins/Terra2/terrapacks/
```

Relevant commands:

```text
/terra2 convert <ID> <input.zip> [namespace:dimension]
/terra2 convert <ID> <input-a.zip;input-b.zip> [namespace:dimension]
/terra2 packs list
/terra2 packs inspect <ID>
/terra2 reload
/terra2reportlog start
```

See `terra2-engine/TERRAPACKS.md` for conversion rules and the 7.0.14 test recipe.

## What 7.0.14 does not claim

The reference build does not claim universal vanilla datapack compatibility. In particular, complete noise/density/surface compilation, arbitrary custom dimensions/biomes, jigsaw/template execution, processor lists, full structure placement, world-scoped native loot execution, gameplay functions/recipes, or complete datapack registry virtualization are not production capabilities of 7.0.14.

Unsupported semantic resources must fail closed or produce `BLOCKED` conversion output; they must not be silently treated as successfully converted.

## Community Packs

Community Packs remain executable through the Terra compatibility backend rather than being converted to the narrow vanilla TerraPack subset. This distinction is important: failure to convert a complex vanilla datapack does not imply that a compatible Terra Community Pack cannot run.

Known working Community Pack configurations are the practical compatibility baseline for ongoing pack-recovery work.

## Validation rule

A successful Gradle/CI build proves compilation and automated contracts only. Real server validation still requires world creation, chunk exploration, save/restart testing and inspection of Terra2 diagnostics.

Do not hot-swap the Terra2 JAR. Replace it while the server is stopped and perform a complete restart.
