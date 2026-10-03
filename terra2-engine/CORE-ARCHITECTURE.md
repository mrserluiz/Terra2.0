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

No vanilla datapack conversion is implemented in this build. Next stages are:
1. Move decoration and structure stages into an explicit core pipeline.
2. Compile Terra noise/biome/palette definitions to neutral plan components, with parity tests.
3. Add a vanilla worldgen parser and compiler with explicit supported-resource reporting.
4. Add server integration tests for each source and custom dimension.

Current world commands and default-off settings remain the same. Look for
`Terra2 core bound <world> to <dimension> using terra:<pack>@<version>` in console.
Only new chunks generate terrain; no existing chunk rewriting or save-layout migration
is performed. Paper owns world storage. Build success does not replace a server test.
