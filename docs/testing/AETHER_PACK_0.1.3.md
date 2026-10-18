# AETHER Community Pack 0.1.3-terra2 validation

Input: supplied AETHER.zip (0.1.2-terra2). Output is a native Community Pack ZIP, not a vanilla datapack or .terrapack conversion.

Confirmed loader error: RANDOM ore locators used `range` instead of required `height`. The repaired pack uses explicit height and amount ranges. Ore materials were already vanilla; names such as AMBROSIUM identify configured features/structures, not mod blocks. Feature IDs and vein structure IDs are now distinct; vein type is explicitly config-ore:ORE.

The density formula now includes a horizontal mask to create void gaps around the floating terrain near Y=96. Golden Oak uses oak foliage for biome tint while retaining birch trunk. Four biomes retained; no mod dungeons/items are claimed.

[Isolated test run 39159308264](https://github.com/mrserluiz/Terra2.0/actions/runs/39159308264): Paper 26.2 build 133, Java 25, unchanged Terra2 engine 7.3.2 from run 39158002948. Startup and restart passed: 4 biomes, 31 land columns and 50 void columns on a 9x9 grid from -512 to 512 with spacing 128, seed 42. UUID/block checkpoint verified across save/restart. This is a sampled shape and persistence check, not proof of visual parity, ore abundance or a complete mod port. Runtime loading parsed the configured ore and tree definitions. A static block-reference audit found only minecraft block states.

Artifact: AETHER-0.1.3-terra2.zip; SHA-256 `2b76f5335b3715d8b2c187fbe929361b007ad93aa77d0f4fe17b7ee32f1c8dd5`. The exact tested ZIP is available in the AETHER-pack-validation artifact of the linked run.

Install exactly one AETHER pack in plugins/Terra2/packs and restart. Test with a new authorized world. Existing bound worlds must not silently change their generation fingerprint. Bedrock custom colors still require the supported Terra2/Geyser bridge.
