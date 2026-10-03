# Terra 2.0 engine migration

Terra 2.0 will be one standalone Paper plugin. It recovers the maintained
parts of the Terra engine, keeps Community Pack compatibility, and adds a
safety layer that controls exactly which worlds may use generation.

## Architecture

| Layer | Recovered source | Terra 2.0 responsibility |
| --- | --- | --- |
| Pack API | `terra2-engine/common/api` | Preserve pack and addon contracts |
| Core runtime | `terra2-engine/common/implementation` | Load registries, packs, configs, and generation stages |
| Built-in addons | `terra2-engine/common/addons` | Supply standard biome, noise, palette, feature, and structure behavior |
| Paper adapter | `terra2-engine/platforms/bukkit` | Connect the engine to Paper's chunk generator and biome APIs |
| Safety control | `terra2-plugin` | Authorize worlds, validate packs, write manifests, and protect the primary world |

The temporary `terra2-plugin` module is the safety/orchestration prototype. Its
world authorization and manifest behavior will be moved into the recovered
Paper platform so the final distribution contains one plugin and one engine.

## Non-negotiable safety rules

1. Generation is disabled after a fresh install.
2. No world is eligible until its exact name and pack ID are explicitly stored.
3. The server's primary world is denied by default, even if a broad rule would
   otherwise match it.
4. Existing worlds are not retrofitted silently. Enabling an existing world
   requires an explicit unsafe/advanced operation and a backup acknowledgement.
5. A `terra2-manifest.yml` is written inside every managed world and checked on
   subsequent starts before generation is attached.
6. A missing pack, addon, incompatible version, or mismatched manifest fails
   closed: the engine does not generate chunks for that world.

## Delivery phases

- **R0 — source recovery:** import the clean upstream engine with provenance
  and retain Community Pack contracts.
- **R1 — modern build:** compile the Paper adapter on Java 25/Paper 26.2 and
  replace obsolete NMS hooks. Validate 26.3 without making it the production
  target.
- **R2 — native pack runtime:** load bundled and installed packs/addons without
  the legacy Terra plugin.
- **R3 — safe world provisioning:** merge the authorization/manifest layer and
  attach the generator only while a named world is first created.
- **R4 — compatibility tests:** generate deterministic test chunks from
  Community Packs and compare biome, height, palette, feature, and structure
  invariants.
- **R5 — datapack adapter:** translate supported datapack worldgen definitions
  into the Terra 2.0 registry model, scoped to explicitly authorized worlds.

## Current status

Modern storage milestone: manifests now use the dimension folder returned by
Paper (`World.getWorldFolder`) for loaded dimensions. Unloaded dimensions are
blocked until the platform can register and resolve them explicitly; no legacy
folder name is guessed. Loaded `minecraft:*` worlds are protected by default.
This is storage safety, not implementation of custom dimension registration.

The first CI run passed the standalone scaffold on Paper 26.2 and 26.3 but the
recovered Gradle build failed during Java 25 bootstrap. CI now runs the existing
Gradle runtime on Java 21 with an explicit Java 25 compilation toolchain.

R0 is implemented in source. The earlier standalone plugin scaffold remains
safe but its placeholder runtime does not generate terrain. Do not install an
engine build on a production server until R1–R3 pass their integration tests.
