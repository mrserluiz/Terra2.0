# В будущем: предметы и добыча по мирам

[Руководство пользователя](README.md) · **Terra2 7.0.14-BETA**

Планируется, но не работает в 7.0.14: добыча по мирам, идентичность сгенерированных сундуков, происхождение предметов, различение природных и установленных блоков, ограничения получения и использования.


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
