# Rencana item dan loot per dunia

[Panduan pengguna](README.md) · **Terra2 7.0.14-BETA**

Direncanakan, belum aktif pada 7.0.14: pengaturan loot per dunia, identitas peti hasil generasi, asal item, pelacakan blok alami dan blok pemain, serta batasan perolehan dan penggunaan.


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
