# Biome und Strukturen erkennen und finden — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Radius: 32..8192 Blöcke.

Der Index wird nach Welt-UUID und Seed unter `plugins/Terra2/structure-index/` alle fünf Sekunden und beim normalen Herunterfahren gespeichert. Alte Packs liefern Feature-IDs aus der Stufe `structures`; Templates und alte Chunks werden nicht rückwirkend rekonstruiert. Native Strukturen liefern Original-IDs. Höchstens 100.000 Positionen pro Welt. Biomsuche auf deinem Y, 32-Block-Raster, höchstens 16.384 Abfragen oder zwei Sekunden zwischen Abfragen. Keine Chunk-Generierung.

Indexierte Strukturanker: `[ID, XYZ]`. Indexlimit erreicht: `true/false`. Nur generierte/indexierte Positionen; kein Nachweis, dass du innerhalb bist.

Stichprobe gefunden: `ID` [X, Y, Z]. Auf deinem Y; 32-Block-Raster, nicht garantiert am nächsten.

Keine Stichprobe vor Radius-/Zeit-/Abfragelimit (N Stichproben). Kein Nachweis der Abwesenheit.
