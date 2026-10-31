# Validação do porte Paper 26.3 — Terra2 7.4.0-BETA

Fonte compilada: `ef928bd3f6f3a441e79c9c3d5db327130de2975c`. [CI completa](https://github.com/mrserluiz/Terra2.0/actions/runs/41208537568), cinco jobs aprovados. Alvo nativo: **Paper 26.3 build 231, Java 25**. O teste do controlador fino em 26.2 não declara compatibilidade do motor 7.4.0 com 26.2.

- JAR: `Terra2-bukkit-7.4.0-BETA.jar`, 14,001,604 bytes.
- SHA-256: `aa7d23fe7221f88383e23a9c01a36996392d2190a9559224c9ed72e967a17834`.
- [Artefato do motor](https://github.com/mrserluiz/Terra2.0/actions/runs/41208537568/artifacts/13045835457); contém somente o JAR do motor.
- Paper SHA-256: `a836a016be7ac52352eb3a1efb6857bb42ee73a6c78d46e292f7e87742edb0e6`.
- Geyser-Spigot oficial 2.12.1 build 1279; SHA-256 `234fa467a32bbc780bd495296eb18fa0fbd7af8dab41f137ed5f7a45aabfbb90`.

## Evidência por etapa

| Etapa | Resultado e alcance |
| --- | --- |
| Build e testes | Gradle compila e testa o motor completo. Foram também executados localmente 23 testes NMS e 6 do leitor NBT durante o porte. |
| Community Packs | Geração HYDRAXIA antes/depois de reinício; OVERWORLD e TARTARUS carregados no teste. Não equivale a cobrir todos os packs existentes. |
| Datapacks nativos | Fixture jigsaw/rotações, processador, loot com origem por mundo e Dungeons and Taverns 5.1.0 no perfil GENERATION_AND_LOOT; criação/reinício e índice de descoberta aprovados. |
| Biomas | CUSTOM e VANILLA_COMPATIBLE, identidades persistentes, releitura sem gerar chunks, edição e proteção dos mundos base. O teste consulta também o resolvedor de chunks descarregados. |
| AETHER 0.2.0 de referência | 31 colunas de ilha, 50 de vazio, 2.709 comparações de densidade/blocos, 3.321 células reais de bioma, cores registradas, nenhuma estrutura vanilla; resultado estável após reinício. |
| Geyser oficial | Registro/pack, codificação de pacotes, água RGB e alfa vanilla, mapeamentos isolados por sessão, preservação de entradas alheias e substituição do cache, edições persistentes e reinício. Captura de sessão; sem renderizador Bedrock. |
| FAWE oficial 2.16.1 | Adaptador cuboide exercitado; não testa uma seleção de jogador real ou brushes do FastAsyncVoxelSniper. |
| Clima | Fonte, reload, preservação de bioma, reinício e remoção opcional da ponte passaram com o receiver de teste. Não garante integração com o binário oficial do AeternumSeasons. |

## Migração real de uma cópia do save 26.2

Foi reutilizado o mundo da [verificação AETHER 0.5.1 / 7.3.5](AETHER_0.5.1_BIOME_PERSISTENCE.md), sem regeneração, com o ZIP do usuário inalterado. O JAR final da CI acima foi copiado **sem patches** para o servidor isolado 26.3. O teste foi repetido após salvar e reiniciar no 26.3.

- Pack SHA-256: `a99911488ed7d09f3a21f18bfcf6a1287426e9eb503cc5cdc3639a13ef5095c1`.
- UUID preservado: `7c496b21-a25e-49e9-ab0a-316ef3ab6911`.
- Chunks `(20,20)`, `(-21,-21)`, `(0,0)`; **4.608 células 4×4×4**, em toda a altura.
- Leitura por snapshot de chunks efetivamente carregados, comparada com o ID esperado. Unload real, recarga e consulta do resolvedor descarregado também verificados.
- Os cinco perfis de cor do pack foram preservados; a amostra gerou Forest e Meadow, não todos os cinco biomas.
- Assinatura dos IDs reais e das cores: `57fbef4f06d3dc6cb2c82936b1e48c66e052d43dd6229ff7c594f76fdad7df94`, idêntica à do save 26.2.

[Resultado da atualização](paper-26.3-migration/upgrade-result.json) · [Resultado do reinício](paper-26.3-migration/restart-result.json) · [Marcadores](paper-26.3-migration/markers.txt) · [Probe](../../terra2-engine/tools/biome-save-probe/BiomeSaveProbe.java).

Durante o porte, uma leitura por World.getBiome após a expiração de tickets retornava o gerador vanilla para chunks descarregados. O 26.3 mantém `ServerLevel.uncachedBiomeResolver` em cache; substituir apenas WorldGenContext não o atualizava. O adaptador agora troca os dois vínculos e reverte ambos em caso de falha. O probe foi corrigido para distinguir o conteúdo salvo (snapshot carregado) da consulta de fallback. Não houve demonstração de corrupção da paleta salva nesse caso.

Essa correção específica do 26.3 não diagnostica retroativamente o pack 0.6.1 ou os chunks afetados do servidor do usuário. O usuário identificou separadamente um problema de registro no pack, cuja versão corrigida ainda precisa de teste próprio.

## Limites e atualização

O teste não usa cliente Java/Bedrock nem o save de produção. Não valida efeitos/quests/funções excluídos do perfil GENERATION_AND_LOOT. A densidade nativa 26.3 usa precisão float; não há promessa de terreno novo bit a bit idêntico ao 26.2. A leitura do save anterior não regenera terreno existente.

Conversões nativas antigas são vinculadas ao DataVersion e precisam de reconversão para 5023. Mantenha backups e respeite manifests/bindings; não substitua silenciosamente o plano de geração de mundos existentes. O importador continua bloqueando formatos sem suporte. O ReimagEND legado testado permanece recusado pelo SNBT end_gateway fora do subconjunto suportado. O componente map_color foi removido pelo jogo e é preservado apenas como dado histórico na conversão.

Geyser 2.11.3 build 1251 não habilitou no Paper 26.3 por incompatibilidade de CraftItemStack. A 7.4.0 usa Geyser oficial 2.12.1 build 1279; a distribuição antiga 7.3.5 / Paper 26.2 mantém sua matriz anterior.
