# TerraPacks and per-world compositions — 7.0.18-BETA

This build implements an immutable local pack format, an asynchronous conversion
workspace and executable composition of one terrain base with additive supported
features. It is not a universal importer for large worldgen datapacks. Existing
Community Packs still execute through the tested Terra compatibility backend.

## Workspace and commands

The plugin creates these directories, but never creates/authorizes a world by default:

- `plugins/Terra2/conversion/input/`: user-supplied datapack folders or ZIPs.
- `plugins/Terra2/terrapacks/<ID>.terrapack`: converted pack ZIP container.
- `plugins/Terra2/conversion/reports/<ID>.json`: conversion results and blockers.

Every input must have `pack.mcmeta` at its root, or inside exactly one enclosing
folder containing all the input files. A collection of several datapacks is refused. Use simple filenames without spaces;
rename only the input ZIP/folder, not namespaced IDs inside it. The uploaded
`terrenos.zip` is a collection of separate packs under `terrenos/datas`; select each
individual pack, rather than treating the entire collection as one datapack.

```text
/terra2 convert <ID> <input.zip> [namespace:dimension]
/terra2 convert <ID> <input-a.zip;input-b.zip> [namespace:dimension]
/terra2 packs list
/terra2 packs inspect <ID>
```

Conversion runs in an async worker, one job at a time. Commands require
`terra2.settings.reload` (OP by default, configurable through permissions). Sources
are not deleted or modified; files/scripts are not executed. Output IDs cannot
silently overwrite an existing pack/report. Convert updates to a new ID.

Two outcomes are intentionally distinct:

- `READY`: the complete supported semantic resource set compiled to executable IR.
  Paper additionally validates block/biome references before binding the world.
- `BLOCKED`: an archive/report preserves the merged resources and source metadata,
  but loading it as a generator is refused. Inspect/report lists the unsupported
  resources. Creating an archive is not a successful complete semantic conversion.

A merge rejects different bytes at the same `data/<namespace>/<resource>` path.
Identical bytes are deduplicated. Original metadata/licenses/readmes are preserved
under provenance; selecting several packs does not imply approval to redistribute
their contents. Version-dependent overlays/filters block compilation rather than
being dropped. Old pack-format numbers alone are not rewritten to claim 26.2 support:
supported flat/feature definitions are translated to the neutral Terra2 model.

## Current executable subset

- Terrain: vanilla `minecraft:flat` with overworld type, vanilla block IDs, fixed
  vanilla biome, explicit empty structure overrides and no vanilla lakes/features.
- Additive feature extension: `minecraft:simple_block`, simple state provider,
  vanilla solid block state, `in_square` + `WORLD_SURFACE_WG` heightmap, optional
  constant `count` and `rarity_filter`. The compiled feature is applied after
  existing world population stages using Paper's LimitedRegion, within its chunk.
- One terrain base, either a legacy Community Pack or a converted flat TerraPack.
  Extensions can add compiled features without replacing terrain or biome queries.

Feature randomness is Terra2's deterministic sampler; exact vanilla PRNG parity and
arbitrary plant/block survival behavior are not claimed. Non-solid feature blocks
are refused by the Paper bridge. Budgets are 32 features/128 placement attempts per
chunk across the composition. Conversion inputs are bounded (8 MiB per resource,
128 MiB combined, 20000 entries); ZIPs are never extracted, symlinks/traversal refused.

Still unsupported: noise/density/surface graphs, custom biome/dimension types,
biome/tag placement filtering, jigsaw/template execution, processor lists,
loot integration, scheduled ticks, functions, recipes and non-worldgen gameplay.
A pack containing any of these semantic resources becomes BLOCKED. They are not
ignored or run globally. Most uploaded packs are therefore still conversion drafts.

## Per-world settings

Preferred explicit list:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_mix_teste:
    packs: [OVERWORLD, Debris]
```

Equivalent compact syntax:

```yaml
worlds:
  terra2_mix_teste:
    pack: OVERWORLD;Debris
```

Only use `pack`, `packs` OR the earlier `datapack`/`dimension` selection in an entry.
The first pack supplies terrain. Each ID must match a loaded Community Pack or a
READY local TerraPack. Repeat IDs, unknown IDs, a second terrain base, duplicate
feature IDs and BLOCKED packs are refused before a generator binding is created.

`pack: OVERWORLD;Dungeons-and-Taverns;Yggdrasil` is recognized as a selection, but
those uploaded full datapacks do not yet compile to READY extensions. It cannot
be used as a working world recipe until their missing stages are ported. Yggdrasil's
own custom dimension is not automatically merged into OVERWORLD. Supporting the
syntax is distinct from supporting every pack's semantics.

For a composition, request the configuration-selected generator:

```text
/mv create terra2_mix_teste normal --generator Terra2:PACKS
```

Single legacy IDs continue to work with `Terra2:OVERWORLD` etc. A world still needs
explicit authorization and generation.enabled. World/vanilla dimension protection
remains enforced. No primary-world datapacks are installed and no global registries
are replaced. Paper supplies the actual world path/key; no save layout is guessed.

## Included executable test, without external packs

Install `Terra2-bukkit-7.0.14-BETA.jar` in place of the previous JAR and restart once.
The plugin creates two example inputs (`flat-demo`, `scatter-demo`) if missing;
it does not automatically convert them. Run these commands separately, waiting for
each conversion's result before the next command:

```text
/terra2 convert Plano flat-demo terra2_demo:flat
/terra2 convert Debris scatter-demo
/terra2 packs inspect Plano
/terra2 packs inspect Debris
```

Add a new disposable world entry to the existing settings:

```yaml
  terra2_mix_teste:
    packs: [Plano, Debris]
```

Then:

```text
/terra2 reload
/terra2reportlog start
/mv create terra2_mix_teste normal --generator Terra2:PACKS
/mv tp terra2_mix_teste
```

Expected: flat grass surface Y=0, with scattered mossy cobblestone on new chunks.
Alternatively use `packs: [OVERWORLD, Debris]` in a different new test world to
check the Terra compatibility backend plus a converted vanilla feature extension.
Do not attach a changed composition to the existing validated test world.

You can also merge the two inputs into one converted TerraPack:

```text
/terra2 convert PlanoComDebris flat-demo;scatter-demo terra2_demo:flat
```

Select `pack: PlanoComDebris` and use the same `Terra2:PACKS` generator marker.
The original `datapack`/`dimension` flat importer remains supported.

A composed world stores its plan/source fingerprints, selected IDs, seed and actual
dimension key in `terra2-generation.json`. Reload refuses a changed active source
or selection; restart checks the manifest before allowing new generation. Already
loaded worlds retain their generators if authorization is removed. Existing chunks
are never rewritten. Hot-swapping the plugin JAR is unsupported.

## Verification and remaining work

CI tests neutral flat conversion from an older schema, asynchronous workspace
contracts, source merge/provenance, legacy-base/feature compatibility, duplicate
terrain/features, blocked functions/structures/secondary-source overlays, and
conflicts/traversal before output publication. Deterministic additive execution is
checked through GenerationManager on a bounded in-memory chunk volume. Bukkit/NMS
and the existing core tests also run. Server creation/exploration must still be tested.

Next work is actual noise/density/surface compilation, version-aware overlay/resource
resolution, custom biome/type provisioning and per-world structure stages (including
jigsaw, NBT templates and loot). The uploaded Tectonic/Terralith/structure collections
are compatibility targets, not packs supported by this release.


## Structure migration diagnostics (7.0.15)

Conversion now reads Java structure NBT (gzip or raw) into platform-independent
palettes, positions, block state indices, block-entity NBT and entity compounds.
It rejects invalid palettes, out-of-bounds or duplicate positions, truncated tags,
trailing data, excessive nesting, oversized arrays and decompression bombs. Limits:
16 MiB decompressed per piece, depth 64, a 2,000,000 total node/element budget
and at most 1,000,000 elements in any single array/list,
64 alternative palettes and 512 per size axis (volume at most 1,000,000).

`structureMigration` in each **new** conversion manifest/report records decoded
pieces, source DataVersion, jigsaw and block/entity counts, pool and loot references,
structure/pool/processor/placement types, and missing custom resources. References
are `LOCAL`, `MISSING_CUSTOM`, or `VANILLA_EXTERNAL_UNVALIDATED`. The latter is
not evidence that the current server registry supplies that ID. Older reports
remain readable but need conversion under a new ID to include this inventory.

`/terra2 packs inspect <ID>` shows the decoded-piece and dependency/error counts.
The inventory is diagnostic; it does not make jigsaw structures READY or migrate
old block/entity data. No commands/entities/loot are executed during inspection.
Original NBT remains byte-for-byte preserved in `resources/`.

The uploaded Dungeons and Taverns 5.1.0 contains 4,894 NBT pieces, 608 pools,
113 jigsaw structures, 34 structure sets, 59 processor lists and 559 loot tables.
It also contains 131 functions and gameplay resources. Its biggest decompressed
piece is 4,240,801 bytes. Finishing that pack requires actual jigsaw assembly,
rotations, both random-spread and concentric-ring placement, biome filters,
processors, NBT data fixing, loot/trial-spawner migration, and a world-isolated
execution bridge. Full compatibility is not implemented in 7.0.15. Functions
must be accounted for rather than silently discarded or installed globally.

Reports and pack archives are built in temporary files. A report-write failure
cannot leave a published pack without its report; archive publication failure
rolls back the newly published report. Existing IDs are never overwritten.

A local Java-reader probe decoded all 4,894 supplied NBT templates (67 source
DataVersions), including the intentional empty `vanilla_structure_remover` piece.
The probe used the same reader with List.getFirst replaced by get(0) for the local
Java 17 runtime; the Java 25 plugin and unit tests are validated separately in CI.
Decoding is not data fixing, jigsaw assembly or an in-server generation test.

## Native template migration (7.0.16)

The Bukkit conversion command now invokes Paper's structure reader and writer,
which use Minecraft's structure DataFixer. Migrated templates are stored separately
in `native-templates/data/...`; original source bytes remain in `resources/`.
`nativeTemplateMigration` records the source and actual server target DataVersion
for each successfully migrated piece. This stage neither registers structures
globally nor changes a world. It runs on the existing conversion worker.

Future DataVersions, incorrect target versions, invalid output, changes to geometry
or loss of entities/jigsaw connectors fail the stage. Any failure discards the
entire migrated template set; partial graphs are never published for execution.
The report remains available with errors and the pack remains BLOCKED. Migration
uses the existing byte limits, including original and derived template bytes.

New Bukkit conversions use the explicit `GENERATION_AND_LOOT` profile: functions,
function tags, advancements and recipes are preserved as provenance/source but
excluded from execution, with every exclusion listed in the report. Loot tables,
predicates, item modifiers, enchantments and trial spawners remain required
resources. Existing manifests without a profile retain FULL validation.

This build does **not** complete Dungeons and Taverns. Jigsaw assembly, native
structure placement (including concentric rings), processors and world-scoped loot
execution are still blocked by the compiler. A `MIGRATED` NBT result does not imply
READY, runtime placement, loot execution or successful restart persistence.
The migration contracts have automated tests; actual DataFixer migration of the
full uploaded pack still requires a running Paper 26.2 conversion test.

## Native execution interfaces and scoped resource validation (7.0.17)

New conversions include `nativeResourceValidation`: the Paper backend decodes
worldgen and loot JSON with Minecraft's codecs, resolves forward pool/loot
references and evaluates source tags in a detached lookup. Missing custom
dependencies and codec failures are recorded in the report. This does not modify
server registries or change BLOCKED to READY.

Resource identifiers use `terra2:<full-source-sha256>/<original-namespace>/<path>`.
Codec type identifiers, jigsaw start connector labels, translations, textures and
item-model identifiers retain their original meaning. Source tags have isolated
aliases. This layer never replaces a vanilla resource.

`NativeWorldExecutor` contains world-bound operations for native placement-state
construction, Structure.generate (including jigsaw expansion),
StructureStart.placeInChunk (including processors) and native loot evaluation with
an invocation-specific resolver. It checks the actual world name and dimension,
rejects the three primary vanilla dimension keys, and refuses structures or loot
dependencies without persistent registry identities. **The executor is not yet
connected to the Bukkit world-generation lifecycle.**

Before enabling the full pack, the platform still needs a complete startup/reload
lifecycle for isolated registry identifiers, scoped migrated templates, vanilla
structure-set overrides, chunk start/reference persistence and live-world
generator installation. It also needs full-pack generation/restart verification.
Dungeons and Taverns therefore remains BLOCKED; this build is not a full-pack
server generation test release.

The future item layer has a tested `WorldLootPolicy` and HMAC-SHA256 origin receipt.
Issuance requires an exact authorized world/dimension, pack and loot table. Drop,
item-spawn and pickup triggers cannot issue origin, and any existing origin marker
prevents reminting. Receipts cannot be reassigned to another world or server key.
This is a policy foundation, not an active item renderer, inventory-transfer ban
or general duplication fix. The platform must persist a random server key and
connect trusted loot creation before applying custom names/lore/item models.

## Native loot checkpoint — 7.0.18-BETA

The native test bootstrap now binds the game's item components with
`BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(provider)` before executing
loot. A bootstrap-only registry leaves item holders unbound and fails when native
loot creates an ItemStack; the live server performs this binding during resource
loading. This fix changes test preparation, not the server's item registries.

Native tests exercise real Minecraft loot execution with nested table references,
count, item name, lore, client item-model IDs and private instrument tags. Client
asset IDs keep their original namespace. Both qualified and unqualified vanilla
enchantment function IDs are recognized when inlining scoped holder sets.

The world executor's placement inventory contains only structure sets defined by
its selected resource bundle. Inherited vanilla sets and sets belonging to other
packs remain available for reference resolution, but are not implicitly activated.

These checks do not finish the live world integration. Dungeons and Taverns remains
**BLOCKED**: scoped templates and persistent registry aliases must still be installed,
generation stages connected and verified in a real world across save/restart. Native
processor/tag behavior must be tested in that context as well. Loot provenance is
still a policy foundation; no active item-origin stamping or player-drop conversion
is claimed by this release.
