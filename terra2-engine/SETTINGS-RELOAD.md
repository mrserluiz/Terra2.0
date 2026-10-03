# Authorization reload (7.0.6-BETA)

After editing `plugins/Terra2/terra2-settings.yml`, run `/terra2 reload`.
The permission `terra2.settings.reload` defaults to operators; the console can also run it.
Malformed settings, missing packs, and changes to a previously requested world's pack are rejected before replacing the active settings.
Reload updates authorization for future generator requests. It does not change the generator of a loaded world, stop its chunk generation, reload packs/addons/biome registries, or change the primary world.
Installing a new JAR still requires a complete server restart. Initial generation remains disabled with no worlds authorized by default.
