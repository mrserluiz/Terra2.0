# AETHER — Terra2 Community Pack 0.1.3-terra2

Adaptation of the supplied AETHER 0.1.2 pack. Inspired by The Aether; not an official mod port. Tree scripts originate from the supplied Hydraxia-derived adaptation; original credits retained in pack.yml.

- Four surface biomes: Skyroot Meadow, Grove, Forest and Woodland.
- Floating-island density around Y=96, now with a horizontal mask creating void gaps.
- Only vanilla block states. AMBROSIUM_VEIN -> gold_ore, ZANITE_VEIN -> diamond_ore, GRAVITITE_VEIN -> deepslate_diamond_ore. These are internal structure IDs, not mod block requirements.
- Separate feature and vein identifiers, qualified ore config type, explicit weighted references.
- Golden Oak uses oak foliage for biome tint; birch trunk retained.
- Structures/dungeons and a complete Aether mod conversion are not included.

Install the ZIP without extracting in plugins/Terra2/packs. Keep exactly one pack with ID AETHER. Restart to load.

Authorize a NEW test world in plugins/Terra2/terra2-settings.yml:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  ethercraft_aether_teste:
    pack: AETHER
```

```text
/mv create ethercraft_aether_teste normal --generator Terra2:AETHER
```

Do not replace the generation plan of an existing bound save. Spawn safety may relocate players; floating islands contain void areas. Test in spectator/creative. Custom colors on Bedrock require the Terra2 resource bridge and official supported Geyser, not just this terrain pack.

Validation is recorded separately in the repository test diagnostics. Visual resemblance to the mod must be accepted in-game.
