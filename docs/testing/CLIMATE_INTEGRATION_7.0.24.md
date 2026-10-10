# Terra2 -> optional bridge -> Aeternum: Paper 26.2 verification

Validated source/builds:

- Terra2 7.0.24-BETA: commit `2b882bb8ccdf19da35d71f8204987249652cc069`, engine job 114304158705 in [run 38083190306](https://github.com/mrserluiz/Terra2.0/actions/runs/38083190306).
- Generic Aeternum consumer 4.5.2-CLIMATE-API-BETA: commit `ff141a727767f43c8c31171b07ce13b2c91790f9`, standalone [run 38083043458](https://github.com/mrserluiz/AeternumSeasons-CustomETHER/actions/runs/38083043458).
- Separate bridge 1.0.0-BETA: commit `4d311b35cf9c7f1af1a721526a953be91f2f4f24`, joint [run 38083807981](https://github.com/mrserluiz/Terra2.0/actions/runs/38083807981).

Paper 26.2 build 133 and Java 25 were used. The HYDRAXIA fixture world was `terra2_climate_smoke`, with seed 42 and a WINTER / minecraft:snowy_plains reference defined exclusively in Terra2 settings. Aeternum local profiles were empty. Its Frost/Heat portal creation was disabled in the isolated test.

## Confirmed

1. The full Terra2 engine builds and independently passes existing native generation, structures, loot and save/restart tests without Aeternum or the bridge.
2. The full Aeternum plugin builds and starts/restarts without Terra2 or the bridge.
3. Terra2 publishes metadata for the explicitly authorized HYDRAXIA world. Main-world/null lookups return no profile.
4. The bridge passes the requested world's identity, UUID, season and reference biome to Aeternum. The main world remains SUMMER while the HYDRAXIA world reports WINTER.
5. No HYDRAXIA profile is copied into Aeternum YAML.
6. A Terra2 reload changes the consumer to SUMMER / minecraft:desert without restarting Aeternum. An invalid season and an attempted primary-world climate profile are refused; the previously accepted snapshot stays active.
7. `/terra2climatebridge disconnect` releases the consumer override; `connect` restores it without unloading plugin classloaders.
8. Both creation and restart report `TERRA2_CLIMATE_BRIDGE_OK biome=terra:hydraxia/hydraxia/drafty_streams`.
9. Starting with the addon JAR removed reports `TERRA2_CLIMATE_NO_BRIDGE_OK`: Terra2 still provides its API, while Aeternum uses its own SUMMER calendar.
10. The packaged engine contains the public API, no Aeternum classes and no bridge implementation. The bridge JAR has only optional plugin dependencies and its management permission defaults to OP.

Final marker: `TERRA2_AETERNUM_INTEGRATION_OK`.

## Limits and observed unrelated failures

The test covers the checked-in HYDRAXIA fixture, not every Community Pack or every third-party climate plugin. Aeternum must offer the generic consumer API; old versions without it leave the bridge inactive. Snow rendering, farming/fauna mappings, online player load, Bedrock clients, live jar replacement and custom portal behavior are not established by this test.

The default sample REIMAGEND pack was rejected by the existing block-entity loader for `minecraft:end_gateway{ExactTeleport:1,exit_portal:[I;100,49,0]}`. HYDRAXIA registered and its climate tests passed. This integration does not fix or claim compatibility with that unsupported NBT payload; full diagnostics are retained in the Actions artifact.

An earlier probe incorrectly disabled/re-enabled a whole Paper plugin in-process; its closed classloader caused a zip-file error on subsequent class lookup. The verified probe uses bridge commands to detach/reconnect metadata while keeping the plugin loaded. New JARs require a normal restart.

## Artifact hashes

| JAR | SHA-256 |
| --- | --- |
| Terra2-bukkit-7.0.24-BETA.jar | b588c55d3497e3717aa273ef4c76b88231a74dc449a9f25b76852e585bec2b7d |
| Terra2AeternumBridge-1.0.0-BETA.jar | e9db9df40c47b4497460af1298f1b21fa6a6a8fa0a67d9a8b94b440ddf177748 |
| AeternumSeasons-4.5.2-CLIMATE-API-BETA.jar | af494873cb40501cf2121e41769cc8c7b6485fdd8b93cc480addeca1805fe92c |
