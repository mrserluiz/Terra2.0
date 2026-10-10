# Installazione e configurazione

[Manuale utente](README.md) · **Terra2 7.0.14-BETA**

Installa il JAR Terra2 in plugins/ a server spento. Riavvia, abilita la generazione e autorizza un nuovo mondo di prova nelle impostazioni mondi di Terra2. Non sovrascrivere il config.yml del motore Terra originale.


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
