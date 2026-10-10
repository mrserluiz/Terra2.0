# À venir : objets et butin par monde

[Manuel utilisateur](README.md) · **Terra2 7.0.14-BETA**

Prévu, non actif en 7.0.14 : butin par monde, identité des coffres générés, provenance des objets, blocs naturels ou placés et restrictions d'obtention et d'utilisation.


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
