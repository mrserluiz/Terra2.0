# Instalacja i konfiguracja

[Podręcznik użytkownika](README.md) · **Terra2 7.0.14-BETA**

Zainstaluj plik JAR Terra2 w plugins/ przy wyłączonym serwerze. Uruchom ponownie, włącz generowanie i autoryzuj nowy świat testowy w ustawieniach światów Terra2. Nie nadpisuj starego config.yml silnika Terra.


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
