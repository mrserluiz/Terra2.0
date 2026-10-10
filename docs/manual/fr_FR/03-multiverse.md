# Créer des mondes avec Multiverse-Core

[Manuel utilisateur](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core crée les mondes ; Terra2 génère le terrain. Autorisez le monde avant de recharger les paramètres et de le créer. Ne changez pas le générateur d'un monde existant.


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
