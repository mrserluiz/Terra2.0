# AETHER — Terra2 Community Pack 0.2.0-terra2

Requires **Terra2-bukkit-7.3.3-BETA / Paper 26.2 / Java 25**. Older engines cannot load the new native density operators; they must refuse rather than substitute generic Skylands.

## Terrain

The terrain uses Minecraft's native `old_blended_noise`, decoded and seeded by the running server's `RandomState`: xz/y scales 0.25, factors 80/160, smear 8, modern random source. The Aether I density graph is translated into the equivalent expression:

`-0.15 + clamp((y-8)/32,0,1) * (-0.1 + clamp((128-y)/72,0,1) * (0.07 + old_blended_noise))`

Interpolation uses world-aligned 8 x 4 x 8 cells, including negative coordinates. The original final squeeze preserves the zero boundary; this pack leaves the interpolated field unsqueezed so Terra's existing 4-block subdivision does not interpolate a nonlinear squeezed field a second time. For this pack's shared density and zero carving/elevation, the solid/air test is equivalent, apart from floating-point decisions exactly at zero. This is not a claim of complete mod parity: material palettes, vegetation and biome placement remain Terra2 implementations.

## Isolation and vegetation

- No vanilla carvers, decoration or structure starts; no villages or structure bearding. These pack settings do not control independent plugins that place their own structures.
- Four biomes have distinct tree placement densities: Forest (grid width 7), Woodland (9), Grove (12), Meadow (24), each with padding 3. Mixtures vary between normal/tall Skyroot and Golden Oak stand-ins.
- Compact tree scripts replace the previous giant borrowed canopy. Trees require solid grass/dirt with air above; placement range follows the islands from Y 8 through 128.
- Biome layout remains a cellular Terra provider, not the original Aether climate parameter table. Vanilla blocks substitute mod materials. The previous artistic colors are retained.
- Ores are not enabled in this terrain-first build. Old ore definitions remain unused as migration references. Aerclouds, Crystal Islands, Quicksoil, original ores/dungeons and Aether II are not implemented here.

## Installation

With the server stopped, replace the engine JAR and leave **one** ZIP with ID AETHER in `plugins/Terra2/packs`. Remove the older AETHER ZIP. Restart. Authorize a **new** world in `plugins/Terra2/terra2-settings.yml`:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  aether_020_teste:
    pack: AETHER
```

```text
/mv create aether_020_teste normal --generator Terra2:AETHER
```

Do not rebind an existing save or modify the primary worlds. Use creative/spectator for initial exploration: there is real void between islands. Bedrock colors use Terra2's supported official Geyser bridge and generated resource pack.

## Provenance

Noise parameters and the mathematical graph were checked against The Aether Team's Aether I `1.21.1-develop`, commit `e07d30e16fbd0f09cc067b39594035e395dcf996`, `AetherDensityFunctions.java` and generated `worldgen/noise_settings/skylands.json`. This is an independent compatibility pack, not an official Aether release. No original mod textures, sounds or models are included. The original project licenses code under LGPL-3.0 and reserves its assets separately. Original pack supplied by EtherCraft; the 0.2.0 tree stand-ins are Terra2 scripts.

See repository `docs/testing/AETHER_PACK_0.2.0.md` for validation and remaining limits.
