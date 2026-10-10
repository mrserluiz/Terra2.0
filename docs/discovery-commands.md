# Localização por mundo — 7.0.23-BETA

Comandos para jogadores com a permissão administrativa `terra2.settings.reload` (OP por padrão):

```text
/terra2 biome
/terra2 structures [raio]
/terra2 locate biome <ID> [raio]
/terra2 locate structure <ID> [raio]
```

Tab sugere os biomas do pack do mundo atual e os IDs de estruturas já indexadas nele. `/terra2 biome` identifica o bioma Terra na posição XYZ atual e mostra também o bioma vanilla usado pelo cliente/F3. Isto não registra cada bioma Terra como um novo ID vanilla.

A busca de biomas consulta o provedor em segundo plano, no Y atual, em anéis quadrados de uma grade de 32 blocos. Raio padrão 4096, permitido 32–8192; até 16.384 consultas ou dois segundos entre consultas. O resultado é uma amostra, sem garantia de ser o bloco mais próximo. Um resultado negativo não prova ausência. Não carrega nem gera chunks. Uma consulta individual do provedor não pode ser interrompida à força; uma busca por jogador é permitida por vez.

`structures` lista até dez âncoras indexadas num raio horizontal padrão de 256 blocos. `locate structure` encontra a âncora indexada mais próxima do ID indicado, dentro do raio. Estas consultas usam exclusivamente o índice de geração do mundo atual: **não são uma implementação completa do `/locate structure` vanilla em chunks ainda não gerados**.

O índice registra starts nativos válidos e features dos Community Packs cuja etapa se chama `structures`. O ID de uma feature indica o configurador que colocou a estrutura, não necessariamente o nome do template. Árvores, minérios e etapas com outros nomes não são indexados. As coordenadas nativas representam o canto mínimo da bounding box; as coordenadas de features representam sua origem de geração. Nenhuma delas comprova que o jogador está dentro da estrutura.

Registros novos são salvos a cada cinco segundos e no desligamento normal em `plugins/Terra2/structure-index/<UUID>.json`, com UUID e seed validados. Cada mundo tem limite de 100.000 registros, informado na resposta. O índice não é uma alteração dos arquivos de chunks. Mundos recriados com UUID novo não herdam posições anteriores. Estruturas de chunks gerados antes desta build não são reconstruídas automaticamente. Uma queda abrupta pode perder os últimos segundos de registros, sem afetar os chunks existentes.

Teste sugerido: criar um mundo autorizado, gerar chunks com estruturas, consultar bioma e estrutura, desligar normalmente, reiniciar e repetir a consulta. Conferir que outros mundos não recebem os registros. O mundo principal continua protegido contra a geração Terra2.
