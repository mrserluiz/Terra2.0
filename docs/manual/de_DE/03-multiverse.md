# Welten mit Multiverse-Core erstellen

[Benutzerhandbuch](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core erstellt Welten; Terra2 generiert das Gelände. Autorisiere die Welt vor dem Neuladen der Einstellungen und dem Erstellen. Ändere nicht den Generator bestehender Welten.


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
