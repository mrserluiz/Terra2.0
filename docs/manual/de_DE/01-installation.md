# Installation und Konfiguration

[Benutzerhandbuch](README.md) · **Terra2 7.0.14-BETA**

Installiere die Terra2-JAR bei gestopptem Server unter plugins/. Starte neu, aktiviere die Generierung und autorisiere eine neue Testwelt in den Terra2-Welteinstellungen. Überschreibe nicht die alte Terra-config.yml.


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
