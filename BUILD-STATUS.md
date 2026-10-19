# Recovered engine build status

## Current reference: 7.3.4-BETA

[CI 39339281309](https://github.com/mrserluiz/Terra2.0/actions/runs/39339281309) passed all jobs: automatic Bedrock session recovery, bounded retry/backoff, persistent world exclusions, registry replacement recovery and incremental full-height loaded-window refresh. Generation, biome persistence and AETHER 0.2.0 remain covered. [Installation, validation and checksum](docs/releases/7.3.4-BETA.md). Paper 26.2 / Java 25; official local Geyser 2.11.3 build 1251. A real Bedrock reconnect/rendering acceptance test remains necessary.

## Previous reference: 7.3.3-BETA

[CI 39176448652](https://github.com/mrserluiz/Terra2.0/actions/runs/39176448652) passed all jobs, including native old-blended noise, Aether I density translation, island/void generation, pack isolation, save/restart, existing biome persistence, official Geyser 2.11.3 build 1251 and the optional climate receiver fixture. See [release notes](docs/releases/7.3.3-BETA.md) and [AETHER validation](docs/testing/AETHER_PACK_0.2.0.md). Native target remains Paper 26.2 / Java 25; Geyser remains official 2.11.3 build 1251. Aether biome layout, vegetation and materials are still compatibility approximations; full mod parity is not claimed.

## Previous reference: 7.3.2-BETA

[CI 39158002948](https://github.com/mrserluiz/Terra2.0/actions/runs/39158002948) passed all jobs: engine build, unit tests, native generation/save/restart, biome persistence, official Geyser 2.11.3 build 1251 and climate fixture. Explicit base colors now receive effective HSV with field-by-field global inheritance. The report/debug shows original, base and final RGB. [Migration, limits and checksum](docs/releases/7.3.2-BETA.md). Bedrock rendering remains a real-client acceptance test; Geyser 2.12 is not supported.

## Previous reference: 7.3.1-BETA

Exact Bedrock color overrides, validated biome IDs, compiled-color reports and in-game debug are available. Engine validation: [39041592458](https://github.com/mrserluiz/Terra2.0/actions/runs/39041592458). Official Geyser 2.11.3 build 1251 startup/restart checks: [39042325373](https://github.com/mrserluiz/Terra2.0/actions/runs/39042325373), reusing the identical engine artifact after a source comparison. Geyser 2.12 changed registry contracts and is not declared compatible. [Release notes and checksum](docs/releases/7.3.1-BETA.md). Real client rendering remains a server acceptance test.

## Previous reference: 7.3.0-BETA

[CI 38354726746](https://github.com/mrserluiz/Terra2.0/actions/runs/38354726746) passed all jobs, including the engine, calibration tests, save/restart and official Geyser integration. [Release notes and checksums](docs/releases/7.3.0-BETA.md). Real WorldEdit selection and Java/Bedrock visual matching remain server acceptance tests.

## Previous reference: 7.0.30-BETA

The standalone engine targets Paper 26.2 / Java 25. [CI 38341188293](https://github.com/mrserluiz/Terra2.0/actions/runs/38341188293) passed engine compilation, native generation/save/restart, biome edit persistence and official local Geyser checks. The 26.3 check covers the thin controller only, not the native engine.

See [cumulative release notes since 7.0.25](docs/releases/7.0.30-BETA.md) for installation, checksums, server acceptance steps and remaining limits. Native biome colors were visually confirmed by the user in 7.0.29; real WorldEdit selection and the new 7.0.30 Bedrock reconnect cycle still require server acceptance.

## Historical recovery milestone

The statements below document the initial recovery stage; they are not the current feature matrix.

The engine distribution uses the recovered Terra API, addon system, and native
Community Pack loader. It does not depend on an installed Terra plugin.

`PackSourceAdapter` is wired into the core pack registry through explicit registration
and Java service providers. Ambiguous adapters fail before loading; adapter failures
are reported with the source path. Native Community Packs retain their original loader.
There is no vanilla datapack translator yet. An adapter must produce a native
`ConfigPack` and must not create dimensions or change save data; the world
controller remains responsible for authorization and attachment.

The recovered Paper plugin now requires `terra2-settings.yml` authorization
for every world/pack pair. The default configuration disables generation and
contains no authorized worlds. Requested generators fail explicitly rather
than silently creating vanilla terrain on rejection.

The recovered engine compiles on Paper 26.2. The internal adapter has been ported:
Identifier/resource keys, villager package, record chunk coordinates, biome effect
records, ambient types, and environment attributes. Legacy biome configuration
fields are translated to the current model; unconfigured vanilla attributes are
inherited. Dry foliage colors and the temperature modifier loader are also preserved.
Tests bootstrap the vanilla runtime, resolve reflection bindings, and verify biome
attribute/climate preservation and explicit legacy color overrides.

Generation authorization is checked both when providing a Bukkit generator and
when injecting the NMS delegate. The primary level-name from server.properties
and loaded minecraft:* dimensions are denied. Injection locks are released even
on failure and a world is marked injected only after success.

The scaffold and recovered engine are separate experimental artifacts and must
not be installed together (both use plugin name Terra2). Full server startup,
Community Pack chunk generation, restart/save integrity, safe dimension provisioning
and merged manifests still require integration validation. Only the scaffold has
been compiled against 26.3; the recovered engine targets 26.2.

CI uses Gradle 9.7.1, matching the minimum Gradle plugin variant declared by
Paperweight 2.0.0-beta.24. The imported wrapper still targets 8.14.1 and cannot
build this engine; until its verified upgrade, use installed Gradle 9.7.1.

A successful scaffold build does not mean the recovered engine is operational.
No production engine release, complete datapack translation, or new dimension
creation is claimed at this milestone. Engine artifacts are available from the
successful GitHub Actions runs under Terra2-engine-experimental-26.2.
