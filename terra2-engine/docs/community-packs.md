# Community Packs — Terra2 7.0.20-BETA

Community Packs use the recovered Terra backend directly. They do not need a
`.terrapack` conversion. Put a pack ZIP or unpacked pack directory in
`plugins/Terra2/packs/`. `pack.yml` must be at the root of that source, as required
by the legacy loader. GitHub repository wrapper ZIPs must be unpacked first.

The manifest `id`, including its case, selects the pack; the archive filename
never selects it. Bundled defaults are not extracted when an installed source
already has the same manifest ID, even under a different filename. Addons, version constraints, YAML references, blocks and pack
content still have to pass the original ConfigPack loader. Discovery is not
registration: rejected sources retain their manifest ID, path and loader cause.
No production code contains special handling for HYDRAXIA or a list of known
Community Pack IDs.

## Select and create a new world

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  hydraxia_teste:
    pack: HYDRAXIA
```

After installing a new pack, restart the server and verify
`Community Pack registered: HYDRAXIA:HYDRAXIA` in the log. Then:

```text
/terra2 packs list
/mv create hydraxia_teste normal --generator Terra2:HYDRAXIA
```

Generation remains disabled by default. Protected and unauthorized worlds are
refused before resolving a pack or creating a generator. Existing chunks are not
rewritten. Adding a pack does not create or authorize a world.

## Resolution and compositions

Each configured ID is resolved independently. A registered Community Pack enters
the legacy backend; an existing converted ID enters TerraPackStore. Unknown IDs
list both source directories and available IDs. A rejected Community Pack retains
its loader failure instead of producing a misleading missing `.terrapack` error.
A collision between registered COMMUNITY and TERRAPACK IDs is rejected; rename
one ID. Duplicate Community manifest keys are rejected before either is loaded.
Namespaced IDs can disambiguate Community Packs with different namespaces.

```yaml
worlds:
  mixed_test:
    packs: [OVERWORLD, MyConvertedOverlay]
```

```text
/mv create mixed_test normal --generator Terra2:PACKS
```

The existing composition rules still apply: a Community terrain base must be
first and additional entries must be supported converted overlays. This change
does not merge multiple legacy terrain bases or make BLOCKED packs executable.

## Reload and diagnostics

`/terra2 reload` rescans pack manifests without replacing live ConfigPacks or
calling the legacy reload that mutates active generators. Newly discovered
sources are marked RESTART_REQUIRED and the command/log explains that a server
restart is necessary for registration. If new settings select an unregistered
source, settings reload is refused; the previous settings remain active.
Changes to existing pack content also require a restart. Metadata discovery never
claims that changed generator content was activated.

`/terra2 packs list` lists registered Community IDs, converted IDs and rejected
or pending sources. GenerationReport and `/terra2reportlog` include discovery
paths, manifest IDs, registration state and loader errors. Conversion commands
continue to require administrative permission, granted to OP by default.

## Validation scope

Automated tests cover ZIP and directory manifests, filename independence,
missing/invalid IDs and YAML, duplicates, missing IDs, converted-only IDs,
collisions, source load rejection, composition and discovery after startup.
The disposable Paper 26.2 probe also checks the actual OVERWORLD and TARTARUS
loaders (with the TARTARUS archive deliberately renamed), reload discovery,
native converted overlay generation, loot and save/restart.

HYDRAXIA is a compatibility test case, not a runtime exception. Its exact
0.2.3-terra2 archive must be tested separately: successful discovery does not
prove that its addons and old Minecraft block states are compatible. Any next
ConfigPack rejection should be fixed in the pack or a justified generic migration,
while preserving the original source and reporting the exact loader cause.
