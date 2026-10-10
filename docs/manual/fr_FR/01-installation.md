# Installation et configuration

[Manuel utilisateur](README.md) · **Terra2 7.0.14-BETA**

Installez le JAR Terra2 dans plugins/ lorsque le serveur est arrêté. Redémarrez, activez la génération et autorisez un nouveau monde de test dans les paramètres des mondes Terra2. N'écrasez pas le config.yml hérité du moteur Terra.


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
