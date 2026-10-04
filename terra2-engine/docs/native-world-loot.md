# Native structures and loot per world

Native overlays retain the original source archive, migrate structure NBT using the
server DataFixer and scope resource identities under the source fingerprint.
Templates are persisted under `plugins/Terra2/native-runtime/` and reinstated
before a configured world can load its chunks. Native structure sets are private
activation lists; they are not installed as global vanilla structure sets.

The native execution gate is enabled only after the isolated Paper integration
test passes. `READY_NATIVE` means the generation-and-loot profile is executable.
Functions, recipes and advancements remain preserved source material and are not
executed by this profile. A native overlay requires a terrain base such as
`OVERWORLD`; it does not itself convert vanilla noise-based terrain.

The bundled compatibility rules adapt legacy entity predicates, time-check
clocks, removed patch features and renamed dripleaf support tags to 26.2. Empty
bed block-entity metadata is removed by Minecraft's DataFixer because 26.2 no
longer has bed block entities. Dungeons and Taverns contains a Waystones mod
block: its empty deepslate waystone becomes a decorative vanilla lodestone.
This adaptation is recorded in `nativeTemplateAdaptations`; it does not add mod
teleportation. Nonempty mod inventories or unrecognized custom data are refused.

## World selection

Generation is disabled by default, with no worlds selected. Example explicit
authorization in `plugins/Terra2/terra2-settings.yml`:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_dnt_test:
    packs: [OVERWORLD, DNT_NATIVE]
    loot:
      enabled: true
      tables:
        "minecraft:chests/simple_dungeon":
          name: "Relic from {world}"
          lore: ["Origin: {dimension}"]
          item-model: aeternum:relic
          items: [minecraft:diamond]
```

Use the original loot-table ID for a converted pack's rule. A missing rule
preserves native loot presentation. Native generated loot still receives signed
provenance when presentation changes are disabled. `item-model` references a
model supplied by the player's resource pack; it does not generate a texture.

Conversion: place the individual datapack ZIP in `conversion/input/`, then run
`/terra2 convert DNT_NATIVE dungeons-and-taverns.zip`. Existing converted IDs
are immutable: use a new ID to reconvert an older blocked output. Check the
conversion report before authorizing or creating the test world.

`/terra2 loot status` reports active loot bindings and the number of signed
origins issued during this server process. Administrative commands require the
existing `terra2.settings.reload` permission, granted to OP by default.

## Provenance and restart

The manager handles newly generated loot-table output, not item pickup or item
drop events. Already marked items are never reissued or decorated. The HMAC key
in `plugins/Terra2/loot/origin.key` survives restart; a damaged key is rejected
rather than silently replaced. Preserve this file with the plugin data backup.

New native resource identities are installed before players join. World
generation then uses Minecraft's structure positioning, jigsaw expansion,
processors and chunk structure-start serialization for the authorized world.
Changing an active world's pack composition is refused.

The CI probe runs the released shaded JAR in a disposable Paper 26.2 server,
converts the exact Dungeons and Taverns 5.1.0 source fingerprint, generates a
deterministic jigsaw fixture with processor output and native chest loot, saves,
restarts a new Java process and checks saved loot provenance and new chunk
generation. Logs, conversion reports and checkpoint evidence are uploaded as a
separate diagnostic artifact. This is a controlled integration test, not a claim
that every individual third-party structure has been explored in-game.
