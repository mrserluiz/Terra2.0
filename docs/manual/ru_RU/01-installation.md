# Установка и настройка

[Руководство пользователя](README.md) · **Terra2 7.0.14-BETA**

Установите JAR Terra2 в plugins/ при остановленном сервере. Перезапустите сервер, включите генерацию и разрешите новый тестовый мир в настройках миров Terra2. Не перезаписывайте старый config.yml движка Terra.


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
