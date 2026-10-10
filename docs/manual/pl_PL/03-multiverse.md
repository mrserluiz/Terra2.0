# Tworzenie światów przez Multiverse-Core

[Podręcznik użytkownika](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core tworzy światy, a Terra2 generuje teren. Autoryzuj świat przed przeładowaniem ustawień i utworzeniem go. Nie zmieniaj generatora istniejących światów.


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
