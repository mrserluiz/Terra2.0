# Создание миров через Multiverse-Core

[Руководство пользователя](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core создаёт миры, а Terra2 генерирует местность. Сначала разрешите мир, затем перезагрузите настройки и создайте его. Не меняйте генератор существующих миров.


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
