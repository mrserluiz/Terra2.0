# Команды миров и переводы

Эта глава относится к 7.0.22-BETA; предыдущие сохраняют указанную эталонную сборку.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Зарегистрированные Community Packs: OVERWORLD, TARTARUS, HYDRAXIA

Для мира terra2_teste разрешён пак OVERWORLD. Настройки сохранены; генерация включена для разрешённых миров.

Миры не создавались и не генерировались заново. Для нового мира: /mv create terra2_teste normal -g Terra2:PACKS

Мир world защищён. Эта команда не снимает защиту. Проверьте YAML вручную; основные ванильные миры остаются заблокированы движком.

Мир terra2_teste загружен. Нельзя заменить его генератор во время работы; сначала безопасно выгрузите мир.

Изменение YAML не обходит защиту движка для основных ванильных миров. Только первый пак может служить базой рельефа Community. Сохранённые разрешения не перезаписывают существующие чанки и не заменяют уже привязанный план генерации.

## Автодополнение, права и безопасность

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Выберите язык сервера в `plugins/Terra2/terra2-settings.yml`. Команды и идентификаторы паков не изменяются. Существующая подробная диагностика сохраняет исходный язык.

```yaml
language: ru_RU
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Обнаружены Community Packs; перезапустите сервер для регистрации: HYDRAXIA

[← README](README.md)
