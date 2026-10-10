# Comandos de mundos e traduções

Este capítulo corresponde à 7.0.22-BETA; os capítulos anteriores mantêm a build de referência indicada.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Validando os packs do mundo terra2_teste em segundo plano. Nenhum mundo é criado ou alterado.

Community Packs registrados: OVERWORLD, TARTARUS, HYDRAXIA

Mundo terra2_teste autorizado com OVERWORLD. Configuração salva; geração ativada para os mundos autorizados.

Nenhum mundo foi criado ou regenerado. Para um mundo novo: /mv create terra2_teste normal -g Terra2:PACKS

O mundo world está protegido. Este comando não remove proteções. Revise o YAML manualmente; os mundos vanilla principais continuam bloqueados pelo motor.

O mundo terra2_teste está carregado. Não é possível trocar seu gerador em execução; descarregue-o com segurança primeiro.

Editar o YAML não contorna a proteção do motor para os mundos vanilla principais. Somente o primeiro pack pode ser uma base de terreno Community. Autorizações salvas não reescrevem chunks existentes nem substituem um plano de geração já vinculado.

## Autocompletar, permissões e segurança

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Escolha o idioma do servidor em `plugins/Terra2/terra2-settings.yml`. Os comandos e IDs dos packs não mudam. Diagnósticos detalhados anteriores mantêm o idioma original.

```yaml
language: pt_BR
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Community Packs descobertos; reinicie o servidor para registrar: HYDRAXIA

[← README](README.md)
