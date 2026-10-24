# Investigação de cores — AETHER 0.5.1 / Terra2 7.3.5

## Resultado observado

O pack enviado `AETHER-0.5.1-MAZE-CHUNK-FIX-TEST(1).zip` foi testado sem alterações em Paper 26.2 build 133, Java 25.0.5 e no JAR publicado do Terra2 7.3.5-BETA. A amostra isolada não apresentou substituição de biomas customizados por biomas vanilla ao salvar, descarregar/recarregar o chunk ou reiniciar o servidor. Isso não demonstra que os chunks já afetados do servidor do usuário estejam intactos, nem testa a renderização Java/Bedrock.

- SHA-256 do ZIP do usuário: `a99911488ed7d09f3a21f18bfcf6a1287426e9eb503cc5cdc3639a13ef5095c1`.
- SHA-256 do JAR: `9c573cbfc1ffe434bd1ddf2dcecddf9c2a22c5ed6b41dedaff02bfc251176c1d`.
- Mundo isolado, seed 42; chunks `(20,20)`, `(-21,-21)`, `(0,0)`; 4.608 células de bioma 4×4×4, em toda a altura do mundo.
- Cada célula real foi comparada ao ID registrado esperado antes e depois do ciclo de descarregamento/carregamento. O teste não edita nem repinta biomas.
- IDs observados na amostra: `terra:aether/aether/skyroot_forest` e `terra:aether/aether/skyroot_meadow`. Os cinco perfis do pack foram verificados no registro do servidor; não se afirma ter gerado todos os cinco biomas na amostra.
- Assinatura dos IDs reais e dos perfis de cor idêntica após reinício: `57fbef4f06d3dc6cb2c82936b1e48c66e052d43dd6229ff7c594f76fdad7df94`.
- Exemplo: SKYROOT_FOREST registra grama `#35E8D3`, folhas `#F6C94C` e água `#A9EEE0`, correspondentes ao YAML enviado.

[Resultado antes do reinício](aether-0.5.1-biome-save/create-result.json) · [Resultado após reinício](aether-0.5.1-biome-save/restart-result.json) · [Marcadores do log](aether-0.5.1-biome-save/markers.txt) · [Probe utilizado](../../terra2-engine/tools/biome-save-probe/BiomeSaveProbe.java).

## Evidência do servidor e hipótese

No log fornecido `2026-10-24-9.log.gz`, o Terra2 carregado é a 7.3.5-BETA. A configuração enviada vincula `aether_dungeons_teste` ao pack AETHER sem ativar VANILLA_COMPATIBLE. O mesmo log contém `[AeternumSeasons] [BiomeSpoof] AUTUMN=AUTO resolved to minecraft:savanna_plateau`, além de conflito de identificador entre WorldEdit e FAWE.

O código disponível para inspeção em `mrserluiz/AeternumSeasons-CustomETHER`, commit `ff141a727767f43c8c31171b07ce13b2c91790f9`, contém um BiomeSpoofAdapter que percorre áreas de jogadores e chama `World.setBiome` com destinos vanilla. A lista `worlds.disabled_season_fx`, em config.yml, exclui mundos desse processamento. Esse repositório contém uma variante de integração; não foi estabelecida equivalência binária com o JAR oficial 4.7 instalado. O log comprova inicialização do recurso, mas não comprova sozinho que ele alterou o chunk mostrado na imagem. Trata-se do principal suspeito, a confirmar por teste de exclusão por mundo e leitura do bioma real.

## Teste no servidor

Na seção `worlds` já existente de `plugins/AeternumSeasons/config.yml`, acrescente `aether_dungeons_teste` à lista `disabled_season_fx`, preservando todos os nomes já presentes. Reinicie e teste uma área ainda não afetada. Não instale uma variante do plugin de estações para este teste.

```yaml
worlds:
  disabled_season_fx:
    - world_the_end
    - aether_dungeons_teste
```

No local, mirando a vegetação, registre `/terra2 biome debug` antes e depois de se aproximar. Se o ID real virar `minecraft:*`, houve alteração do bioma no servidor; se o ID e as cores de registro continuarem customizados, a investigação passa ao envio/renderização no cliente. Um chunk guarda a referência ao bioma; as cores vêm do registro carregado a partir do pack. Biomas já substituídos podem exigir restauração de backup original ou uma edição localizada autorizada; não é seguro reconstruí-los indiscriminadamente com o gerador atual.

## Limites

Teste sem cliente conectado e sem AeternumSeasons, FAWE, FastAsyncVoxelSniper ou Geyser. Não confirma conflito por reprodução conjunta nem compatibilidade de configuração do binário instalado. Os chunks das estruturas Bronze não foram incluídos nesta amostra. A leitura imediata seguida de unload no mesmo tick mantém tickets temporários do Paper; o probe retorna ao scheduler e aguarda a expiração antes de comprovar unload real.
