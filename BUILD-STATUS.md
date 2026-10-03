# Recovered engine build status

The engine distribution uses the recovered Terra API, addon system, and native
Community Pack loader. It does not depend on an installed Terra plugin.

`PackSourceAdapter` is an additive API boundary for future external formats.
There is no vanilla datapack translator yet. An adapter must produce a native
`ConfigPack` and must not create dimensions or change save data; the world
controller remains responsible for authorization and attachment.

The recovered Paper plugin now requires `terra2-settings.yml` authorization
for every world/pack pair. The default configuration disables generation and
contains no authorized worlds. Requested generators fail explicitly rather
than silently creating vanilla terrain on rejection.

Build validation is in progress. The scaffold and recovered engine are separate
experimental artifacts and must not be installed together (both use plugin
name Terra2). NMS bindings still need migration and runtime validation on 26.2.
Changing only the supported-version list would not prove compatibility.

CI uses Gradle 9.7.1, matching the minimum Gradle plugin variant declared by
Paperweight 2.0.0-beta.24. The imported wrapper still targets 8.14.1 and cannot
build this engine; until its verified upgrade, use installed Gradle 9.7.1.

A successful scaffold build does not mean the recovered engine is operational.
No production engine release or world creation is claimed at this milestone.
