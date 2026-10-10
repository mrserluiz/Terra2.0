# Komendy i rozwiązywanie problemów

[Podręcznik użytkownika](README.md) · **Terra2 7.0.14-BETA**

Sprawdź logi, raporty Terra2 i raporty konwersji. Testuj nowe chunki i zapis po restarcie. Nie wymieniaj pliku JAR podczas pracy serwera.


```text
/terra2 reload
/terra2 convert <ID> <input.zip> [namespace:dimension]
/terra2 convert <ID> <input-a.zip;input-b.zip> [namespace:dimension]
/terra2 packs list
/terra2 packs inspect <ID>
/terra2reportlog start
/mv create terra2_teste normal -g Terra2
/mv create terra2_mix_teste normal --generator Terra2:PACKS
/mv tp terra2_teste
```

```text
plugins/Terra2/reports/
plugins/Terra2/conversion/reports/<ID>.json
```
