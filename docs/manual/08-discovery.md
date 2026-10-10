# Identificar e localizar biomas e estruturas — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Raio: 32..8192 blocos.

O índice é salvo por UUID e seed do mundo em `plugins/Terra2/structure-index/`, a cada cinco segundos e no desligamento normal. Packs antigos mostram os IDs das features colocadas com sucesso na etapa `structures`; templates e chunks antigos não são reconstruídos retroativamente. Estruturas nativas mostram seus IDs de registro originais. São mantidas até 100.000 posições por mundo. A busca de biomas usa o seu Y, grade de 32 blocos e até 16.384 consultas ou dois segundos entre consultas. Não gera chunks.

Âncoras de estruturas indexadas: `[ID, XYZ]`. Limite do índice atingido: `true/false`. Apenas posições geradas/indexadas; âncoras não comprovam estar dentro.

Amostra encontrada: `ID` [X, Y, Z]. No seu Y; grade de 32 blocos, sem garantir o mais próximo.

Nenhuma amostra antes do limite de raio/tempo/consultas (N amostras). Isso não comprova ausência.
