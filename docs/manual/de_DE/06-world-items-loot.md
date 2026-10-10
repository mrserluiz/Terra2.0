# Geplant: weltbezogene Gegenstände und Beute

[Benutzerhandbuch](README.md) · **Terra2 7.0.14-BETA**

Geplant, nicht aktiv in 7.0.14: weltbezogene Beute, Identität generierter Truhen, Gegenstandsherkunft, natürliche und platzierte Blöcke sowie Erwerbs- und Nutzungsregeln.


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
