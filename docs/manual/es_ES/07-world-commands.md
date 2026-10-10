# Comandos de mundos y traducciones

Este capítulo corresponde a 7.0.22-BETA; los anteriores conservan la build de referencia indicada.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Validando los packs del mundo terra2_teste en segundo plano. No se crea ni modifica ningún mundo.

Community Packs registrados: OVERWORLD, TARTARUS, HYDRAXIA

Mundo terra2_teste autorizado con OVERWORLD. Configuración guardada; generación activada para los mundos autorizados.

No se creó ni regeneró ningún mundo. Para uno nuevo: /mv create terra2_teste normal -g Terra2:PACKS

El mundo world está protegido. Este comando no elimina protecciones. Revisa el YAML manualmente; los mundos vanilla principales siguen bloqueados por el motor.

El mundo terra2_teste está cargado. No se puede cambiar su generador en ejecución; descárgalo de forma segura primero.

Editar el YAML no evita la protección del motor para los mundos vanilla principales. Solo el primer pack puede ser una base de terreno Community. Las autorizaciones guardadas no reescriben chunks existentes ni sustituyen un plan de generación ya vinculado.

## Autocompletado, permisos y seguridad

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Selecciona el idioma del servidor en `plugins/Terra2/terra2-settings.yml`. Los comandos y los IDs de los packs no cambian. Los diagnósticos detallados anteriores conservan su idioma original.

```yaml
language: es_ES
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Community Packs detectados; reinicia el servidor para registrarlos: HYDRAXIA

[← README](README.md)
