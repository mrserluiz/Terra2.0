# Futuro: oggetti e loot per mondo

[Manuale utente](README.md) · **Terra2 7.0.14-BETA**

Previsto, non attivo in 7.0.14: loot per mondo, identità dei forzieri generati, provenienza degli oggetti, blocchi naturali o piazzati e restrizioni di ottenimento e uso.


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
