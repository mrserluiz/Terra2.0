# Futuro: objetos y botín por mundo

[Manual del usuario](README.md) · **Terra2 7.0.14-BETA**

Planificado, no disponible en 7.0.14: botín por mundo, identificación de cofres generados, procedencia de objetos, bloques naturales frente a colocados y restricciones de obtención y uso.


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
