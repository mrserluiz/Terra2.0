# Terra2 core: first executable migration (7.0.9-BETA)

The standalone `common/implementation/terra2-core` module owns generation routing.
Its contracts import neither Terra nor Bukkit nor NMS. GenerationContext carries an
exact target, seed and exclusive height bounds; BlockVolume abstracts platform state
storage. GenerationPlan identifies source, ID and version and contains an executable
GenerationProgram. GenerationManager has an empty registry by default.

The existing Paper authorization gate still reads terra2-settings.yml. Only an
explicitly authorized generator request creates a core binding. WorldInit binds the
actual Paper dimension key, without guessing save paths. Primary world names and
vanilla built-in dimension keys are rejected. Replacing a plan, dimension, seed or
height bounds of an active binding is rejected. Different worlds receive independent
plans and contexts; native terrain, column samples and biome queries enter the core.

The first Terra adapter compiles a loaded ConfigPack to an executable core plan.
It preserves the original noise generator and biome provider as a compatibility
backend. This is behavioral integration, not a full translation of every YAML node
into a new intermediate representation. Terra decoration/population stages, palettes,
biome registration and vanilla structures still use the existing platform path.
The core facade blocks legacy pack hot swaps that would bypass immutable bindings.

7.0.13 adds the first executable vanilla datapack importer: isolated folder/ZIP
reading and strict flat-definition compilation into neutral GenerationPlan contracts.
The Paper flat adapter uses GenerationManager<BlockData, Bukkit Biome>; Terra uses
GenerationManager<Terra BlockState, Terra Biome>. Both use the same standalone core
implementation, with source-exclusive authorization at the plugin gate. No global
vanilla registry/resource changes are made. See DATAPACK-ADAPTER.md for the narrow
supported subset, manifest behavior and test commands. Noise/datapack structures
are not yet supported. Next stages are:
1. Move decoration and structure stages into an explicit core pipeline.
2. Compile Terra noise/biome/palette definitions to neutral plan components, with parity tests.
3. Add a vanilla worldgen parser and compiler with explicit supported-resource reporting.
4. Add server integration tests for each source and custom dimension.

Current world commands and default-off settings remain the same. Look for
`Terra2 core bound <world> to <dimension> using terra:<pack>@<version>` in console.
Only new chunks generate terrain; no existing chunk rewriting or save-layout migration
is performed. Paper owns world storage. Build success does not replace a server test.

7.0.14 adds an immutable local TerraPack format, async conversion and composition.
GenerationProgram now has an optional decoration stage; GenerationManager routes it
with the same target/seed/height guards. Legacy terrain keeps its existing population
stages, followed by compiled simple-block extension stages through LimitedRegion.
Converted flat plans use the same neutral composition contract. TerraPacks preserve
source/provenance and expose READY vs BLOCKED explicitly. See TERRAPACKS.md; universal
noise, biome and structure conversion remains pending, not silently approximated.
