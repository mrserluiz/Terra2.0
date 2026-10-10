# Installation and configuration

[User manual](README.md) · **Terra2 7.0.14-BETA**

Install the Terra2 JAR in plugins/ while the server is stopped. Restart, then enable generation and authorize a new test world in the Terra2 world-settings file. Do not overwrite the legacy Terra engine config.yml.


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
