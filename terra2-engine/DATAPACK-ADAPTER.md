# Vanilla datapack adapter — 7.0.13-BETA, first executable stage

The validated 7.0.12 Community Pack path is preserved. This adds a separate,
read-only vanilla importer: folder/ZIP -> validated immutable flat definition ->
GenerationPlan -> GenerationManager -> Paper chunk adapter. No Terra ConfigPack,
noise generator, addon or NMS injection is used for imported flat terrain.

## Current supported subset

Minecraft/Paper 26.2 (data format 107.1), `data/<namespace>/dimension/<id>.json`,
`minecraft:overworld` dimension type, `minecraft:flat` generator, vanilla block IDs,
fixed vanilla biome, layers from the actual world minimum Y. Features/lakes must
be false; `structure_overrides` must explicitly be `[]`. No implicit noise,
structures or decorations are substituted. Unknown resources/fields and incompatible
format ranges are refused. Pack metadata overlays/filters are not implemented.

This is **not universal datapack support**. Noise settings, density functions,
surface rules, custom biomes/types, configured/placed features, structure sets,
templates, tags, functions, recipes and block entities remain unsupported. Most
large worldgen packs will therefore be refused. `inspect` lists unsupported files;
`inspect <file> <dimension>` also validates the selected generator/settings.

The source dimension ID selects the imported definition. It does not register that
ID in the server's global dimension registry. Paper/Multiverse selects the actual
world dimension key, which the core binds and records. Save paths are obtained
from Paper's World object; no `world/dimensions` path is invented or migrated.
This release creates a managed extra world via Multiverse, not a new globally
registered datapack dimension type.

## Test using the included demo

1. Install `Terra2-bukkit-7.0.13-BETA.jar` in place of the previous engine JAR;
   keep exactly one Terra2 plugin. A JAR update requires one complete restart.
2. The plugin installs the included demo into
   `plugins/Terra2/datapacks/terra2-flat-demo/` if missing; it does not authorize
   or create a world. The source is also in `examples/datapacks/terra2-flat-demo/`.
   For other compatible sources copy a folder or ZIP with pack.mcmeta at its root. Do **not** put the example
   or imported pack into the primary world's datapacks directory.
3. Add the explicit test-world source alongside existing Community Pack entries:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste3:
    pack: OVERWORLD
  terra2_dp_teste:
    datapack: terra2-flat-demo
    dimension: terra2_demo:flat
```

4. Run, as OP or with `terra2.settings.reload` permission:

```text
/terra2 datapack list
/terra2 datapack inspect terra2-flat-demo terra2_demo:flat
/terra2 reload
/terra2reportlog start
/mv create terra2_dp_teste normal --generator Terra2:DATAPACK
/mv tp terra2_dp_teste
```

The demo surface is Y=0, plains, with 1 bedrock + 60 stone + 3 dirt + 1 grass.
It intentionally has no decoration/structures/mobs generation. Explore newly
created chunks, verify flat terrain and the `terra2-generation.json` manifest
inside the world folder, then `/terra2reportlog stop`. The server test is still
required; CI validates contracts/parsing/build only.

## Isolation and persistence

Default configuration is still disabled and worlds are empty. Each entry must
choose exactly one source, `pack` or `datapack`. World/pack authorization and
primary-world/vanilla-dimension protection apply to both adapters. Active worlds
cannot switch source during reload. Plans snapshot source bytes and are identified
by SHA-256; edits do not mutate an active plan. Reload refuses altered active source
content. On restart the existing manifest must match the source, selected dimension,
world seed and actual dimension key before the generator can run. Replacing a
source requires a new disposable authorized world, not a silent migration.

No existing chunks are rewritten. Do not attach this generator to an existing
ordinary world: mixing old chunks and flat chunks is not a conversion operation.
The importer never runs mcfunction commands or installs global resource overrides.
ZIPs are never extracted. Per-file/total/entry limits, path checks and symbolic-link
rejection bound input loading. Per-world compilation happens once; no file IO is
performed in chunk-generation workers.

## Next stages

1. Compile vanilla density functions/noise settings and surface rules with output
   parity tests, using isolated resource resolution (including tags/references).
2. Compile custom biomes and feature/structure stages, with explicit dependency
   reporting and cycle detection rather than global vanilla registry replacement.
3. Add custom dimension-type provisioning and native codecs where necessary.
4. Validate each expanded subset on a disposable Paper 26.2 server before claiming
   compatibility with real large datapacks. Do not change default authorization.

References: Minecraft 26.2 technical release notes (107.1) and Paper datapack
lifecycle documentation. Paper's bootstrap datapack loading is a global mechanism;
this importer deliberately compiles selected definitions to private core plans.
