# AETHER 0.2.0 / Terra2 7.3.3-BETA

Implementation under validation. Do not treat a successful compile as visual acceptance.

The engine adds reusable `MINECRAFT_OLD_BLENDED_NOISE` and `MINECRAFT_INTERPOLATED` sampler types. Native noise uses the Paper codec plus RandomState; there is no OpenSimplex approximation or silent fallback on unsupported platforms. The interpolation cache is worker-local and bounded; seeded density functions have a bounded cache and retain no world/chunk handles.

Aether I source: [The Aether Team, commit e07d30e](https://github.com/The-Aether-Team/The-Aether/tree/e07d30e16fbd0f09cc067b39594035e395dcf996). Tests retain the generated Skylands density configuration as a reference, replace only unavailable material/surface definitions, resolve the original named base-noise function, and compare against the server-decoded graph at cell corners across seeds and both vertical gradients. Interpolation tests cover negative coordinates, seeds and squeeze ordering.

`minecraft.vanilla-features: false`, `minecraft.vanilla-structures: false`, and `minecraft.beard.enable: false` isolate this pack. Existing packs keep their previous default behavior. Vanilla carvers are already no-ops in this adapter; the old AETHER pack did not define a cave sampler. Therefore the reported caves cannot be attributed to vanilla carvers solely from the screenshot; organic density cavities or third-party generators remain possible. Independent structure plugins need their own world exclusions.

The real Paper probe creates an authorized AETHER world, samples island and void columns over a 1024-block span, checks all four biomes occur in a larger provider scan, verifies zero native structure starts in the sampled chunks and no blocks below the lower gradient, records logs by biome, saves, then restarts and compares a UUID/terrain/biome digest. It also checks that the primary biome remains vanilla. These are sampled assertions, not exhaustive proof for every possible seed.

Terrain geometry targets the original density zero boundary. The sign-preserving final squeeze is omitted before Terra's second interpolation, preserving a piecewise trilinear density instead of interpolating it nonlinearly twice. This does not port Aether biome climate placement, custom block behavior, mobs, original tree features, loot or dungeons. Four vegetation configurations are distinct approximations; visual inspection is still required.

Use a new world; this pack changes generation and its manifest fingerprint. Engine Paper target remains 26.2; native 26.3 is not claimed. Geyser support remains official 2.11.3 build 1251; no Geyser fork or 2.12 support is introduced.
