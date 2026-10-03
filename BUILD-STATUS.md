# Recovered engine build status

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
