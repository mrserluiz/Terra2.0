# Creare mondi con Multiverse-Core

[Manuale utente](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core crea i mondi; Terra2 genera il terreno. Autorizza il mondo prima di ricaricare le impostazioni e crearlo. Non riassegnare generatori ai mondi esistenti.


```yaml
worlds:
  terra2_teste:
    pack: OVERWORLD
  terra2_mix_teste:
    packs: [OVERWORLD, Debris]
```

```text
/terra2 reload
/mv create terra2_teste normal -g Terra2
/mv tp terra2_teste
/mv create terra2_mix_teste normal --generator Terra2:PACKS
```
