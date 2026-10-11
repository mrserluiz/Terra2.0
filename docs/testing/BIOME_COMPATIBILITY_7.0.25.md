# Terra2 7.0.25-BETA — identidade própria persistente e projeção vanilla

## Implementação

- Projeção imutável por mundo, opt-in `VANILLA_COMPATIBLE`, com referência vanilla do Community Pack e substituições explícitas opcionais.
- Provedor nativo retorna holders vanilla; o núcleo e os estágios Terra continuam usando os biomas do pack.
- O getter de features do gerador evita árvores/minérios vanilla extras, preservando a etapa de estruturas e os populadores do Terra2.
- Registro 3D em PDC do chunk, paleta/indexação 4×4×4, UUID, seed, coordenadas, altura, pack, fingerprint, schema e CRC.
- API `WorldBiomeService` lê registros de chunks já carregados. `/terra2 biome` usa o registro salvo diretamente no modo compatível, sem consultar novamente o provedor de geração.
- Preflight do manifesto usa o diretório real da dimensão do Paper 26.2 e ocorre antes da criação de chunks. Recusa regiões existentes sem manifesto, divergências, reutilização do gerador em outro mundo e dimensões principais.
- Reload não permite trocar a projeção de um mundo já vinculado. Registros inválidos, de outro UUID, de coordenadas/altura incompatíveis ou com fingerprint diferente retornam vazio, sem reconstrução.

## Evidências confirmadas no teste real

O fixture usa HYDRAXIA, seed 42, Paper 26.2 build 133, mundos separados compatível e CUSTOM. Em dois boots, confere:

1. `World#getBiome()` devolve `minecraft:frozen_river` onde o motor identifica `terra:hydraxia/hydraxia/drafty_streams`.
2. Todas as alturas da coluna amostrada possuem uma referência vanilla correspondente.
3. 1.368 amostras de blocos coincidem com o mundo CUSTOM de controle, incluindo estados dos blocos.
4. Identidade e SHA-256 do payload persistente permanecem iguais após salvamento/reinício.
5. Coordenadas negativas, altura inválida e leitura de chunk distante sem carregá-lo.
6. Identidade própria não muda quando um plugin modifica o bioma Bukkit.
7. Corrupção, UUID estrangeiro e faixa de altura incorreta não são aceitos.
8. Mudança de modo por reload é recusada e mantém a configuração ativa anterior.
9. Reutilizar o gerador com o mundo principal é recusado antes de escrita.

Os testes unitários cobrem codec, configurações e manifesto, incluindo recusa de mundos antigos sem alterar arquivos de região. O teste nativo existente também verifica conversão de datapacks, montagem jigsaw, processadores, loot, comandos e reinício no modo CUSTOM.

## Correção durante a implementação

O primeiro teste rejeitou a build ao comparar blocos entre os dois modos. Desativar toda a etapa de decoração vanilla também suprimia a colocação de estruturas. O adaptador foi alterado para manter essa etapa e controlar apenas o getter de features vanilla. A comparação passou após a correção; o JAR que falhou não foi disponibilizado como resultado final.

## Limites

É uma beta opt-in para Community Packs, sem overlays convertidos nesse modo. Não converte mundos existentes, não fornece um controlador adicional de spawn e não preserva os efeitos visuais customizados do bioma registrado. O fingerprint usa versão e catálogo do pack/referências, não todos os arquivos internos editados sem mudança de versão.

O teste não demonstra compatibilidade universal com plugins. Em particular, a referência vanilla não força uma estação no calendário do AeternumSeasons oficial. Plugins podem continuar alterando biomas; os registros próprios do Terra2 permanecem separados.

A prova final carrega os chunks de teste de forma assíncrona antes de comparar os dados. Ainda houve avisos de 10/15 segundos do watchdog durante a escolha síncrona de spawn pelo Paper ao criar os dois mundos consecutivamente; a geração terminou, salvou e reiniciou com sucesso. Esse custo inicial precisa ser avaliado na hospedagem. O teste não é um benchmark de TPS nem uma promessa de geração sem pausas.

O pack de exemplo REIMAGEND ainda registra `LOAD_FAILED` por NBT de `minecraft:end_gateway` (`exit_portal`). É uma limitação anterior do loader, fora desta mudança; a validação de projeção usa HYDRAXIA.

## Build final e resultados

- Fonte: [`2d3d468ed42f12ff650b3da03e248430aefe795a`](https://github.com/mrserluiz/Terra2.0/commit/2d3d468ed42f12ff650b3da03e248430aefe795a).
- [Workflow completo 38121349293](https://github.com/mrserluiz/Terra2.0/actions/runs/38121349293): **SUCCESS**.
- Compilação/testes do motor, integração nativa, projeção com persistência e protocolo de clima opcional: aprovados. O protocolo de clima continua usando um fork como fixture, não comprova integração de perfis de estação com Aeternum oficial.
- Projeção: `terra:hydraxia/hydraxia/drafty_streams` → `minecraft:frozen_river`.
- 1.368 amostras de blocos equivalentes ao controle, confirmadas nos dois boots.
- [Artifact do motor](https://github.com/mrserluiz/Terra2.0/actions/runs/38121349293/artifacts/11695343432): um único JAR, sem outro gerador ou Aeternum incluído.
- [Diagnósticos da geração](https://github.com/mrserluiz/Terra2.0/actions/runs/38121349293/artifacts/11695942231).
- JAR: `Terra2-bukkit-7.0.25-BETA.jar`, 13871991 bytes; SHA-256 `9d937569643df2578e958da6cd443eeaf658941b15fba1e9712255f47d2e4979`.
