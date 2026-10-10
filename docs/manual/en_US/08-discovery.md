# Biome and structure discovery — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Radius: 32..8192 blocks.

The index persists by world UUID and seed in `plugins/Terra2/structure-index/`, every five seconds and on normal shutdown. Legacy packs expose successful feature IDs from the `structures` stage; templates and older chunks are not retroactively reconstructed. Native structures expose their original registry IDs. A maximum of 100,000 placements is retained per world. Biome searches sample at your Y, using a 32-block grid, with a limit of 16,384 queries or two seconds between queries. They do not generate chunks.

Indexed structure anchors: `[ID, XYZ]`. Index limit reached: `true/false`. Only generated indexed placements; anchors do not prove you are inside.

Sample found: `ID` [X, Y, Z]. At your Y; 32-block grid, not exact nearest.

No sample found before the radius/time/budget limit (N samples). This does not prove absence.
