# Future world-specific items and loot

[User manual](README.md) · **Terra2 7.0.14-BETA**

Planned, not active in 7.0.14: per-world loot routing, trusted generated-chest identities, item provenance, natural versus player-placed blocks, and acquisition/use restrictions.


```yaml
# FUTURE EXAMPLE ONLY — NOT SUPPORTED IN 7.0.14
world-loot:
  aeternum_aether:
    minecraft:chests/simple_dungeon:
      mode: REPLACE
      with: AETHER:DUNGEON
items:
  AETHER:DUNGEON_KEY:
    obtain-only-in: [aeternum_aether]
    usable-in: [aeternum_aether]
```
