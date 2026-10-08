# Terra 2.0

**Idiomas:** [English](README.md) | Português (Brasil)

Terra 2.0 é uma continuação independente e modernização do motor de geração de mundos Terra para Paper, com foco em **geração independente por mundo**, **compatibilidade com Community Packs**, isolamento seguro dos mundos e um caminho para suporte nativo a datapacks do Minecraft.

> **Build de referência atual:** `Terra2-bukkit-7.0.14-BETA.jar`

O Terra 2.0 não é mais apenas uma estrutura de recuperação. A linha 7.0.14 já possui um núcleo de geração executável, backend de compatibilidade com Terra Community Packs, um primeiro importador de datapacks vanilla, TerraPacks locais e composição de packs por mundo.

## O que funciona na 7.0.14

- Terra Community Packs podem ser selecionados independentemente para cada mundo autorizado.
- A geração original do Terra continua disponível através do backend de compatibilidade.
- A geração é roteada pelo núcleo independente do Terra2 e por vínculos imutáveis de mundo.
- A autorização de mundos é explícita e a geração permanece desativada por padrão.
- Os mundos vanilla principais permanecem protegidos.
- Um primeiro importador de datapacks vanilla compila o subconjunto suportado de `minecraft:flat`.
- Arquivos locais `.terrapack` distinguem conversões `READY` de `BLOCKED`.
- Um mundo pode combinar uma base de terreno com packs de features adicionais suportados.
- A conversão é assíncrona e preserva os dados de origem e proveniência.
- Manifests e fingerprints de geração protegem mundos contra alterações silenciosas de packs/fontes.
- Captura de diagnóstico está disponível através de `/terra2reportlog`.

## Community Packs

Packs no formato Terra continuam sendo o principal caminho de compatibilidade. Um mundo pode selecionar diretamente um único pack legado:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]

worlds:
  terra2_teste:
    pack: OVERWORLD
```

Crie o mundo com Multiverse utilizando o ID configurado:

```text
/mv create terra2_teste normal -g Terra2
```

A sintaxe exata do gerador pode depender do plugin responsável pelo gerenciamento de mundos e do caminho de configuração escolhido no Terra2. Mundos existentes não devem ser associados novamente a um plano de geração diferente.

## TerraPacks e composição

A 7.0.14 introduz um formato TerraPack local e imutável e um modelo de composição. Uma base de terreno pode ser combinada com features adicionais suportadas:

```yaml
worlds:
  terra2_mix_teste:
    packs: [OVERWORLD, Debris]
```

Para composição explícita:

```text
/mv create terra2_mix_teste normal --generator Terra2:PACKS
```

Comandos de conversão:

```text
/terra2 convert <ID> <input.zip> [namespace:dimension]
/terra2 convert <ID> <input-a.zip;input-b.zip> [namespace:dimension]
/terra2 packs list
/terra2 packs inspect <ID>
/terra2 reload
```

O conversor da 7.0.14 é propositalmente rigoroso. Recursos sem suporte produzem um TerraPack `BLOCKED` em vez de serem ignorados silenciosamente.

Consulte [TERRAPACKS.md](terra2-engine/TERRAPACKS.md) para o subconjunto exato suportado e o procedimento de testes.

## Modelo de segurança

O Terra2 não associa geração globalmente. O mundo precisa ser explicitamente autorizado e vinculado a um plano de geração. O mundo principal do servidor e as dimensões vanilla são protegidos por padrão. Vínculos ativos rejeitam alterações na fonte, seleção de packs, dimensão, seed ou limites de altura.

Somente chunks novos são gerados. O Terra2 não reescreve chunks existentes.

## Estrutura do projeto

- `terra2-engine/` — motor Terra recuperado, núcleo independente de geração Terra2 e integração com Paper.
- `terra2-engine/common/implementation/terra2-core/` — contratos independentes da plataforma para roteamento e composição da geração.
- `terra2-engine/TERRAPACKS.md` — referência de conversão e composição de TerraPacks.
- `terra2-engine/CORE-ARCHITECTURE.md` — arquitetura do núcleo executável e marcos das versões.
- `terra2-engine/DIAGNOSTICS.md` — diagnósticos de crashes, travamentos e captura do console.
- `ENGINE-MIGRATION.md` — histórico da migração, regras de segurança e roadmap.
- `BUILD-STATUS.md` — estado atual da build de referência e suas limitações.

## Limitações atuais

A 7.0.14 **não é um conversor universal de datapacks vanilla**. Grafos complexos de noise/density/surface, tipos personalizados de biomas/dimensões, execução de jigsaw/templates, processors, loot isolado por mundo e outros recursos avançados ainda não estão completos nessa build de referência.

Terra Community Packs e conversão de datapacks vanilla são caminhos distintos: Community Packs utilizam o backend de compatibilidade; recursos vanilla suportados são compilados para o modelo neutro de geração do Terra2.

O repositório pode conter notas de desenvolvimento posteriores à 7.0.14 sobre estruturas, NBT e loot. Elas não devem ser interpretadas como funcionalidades presentes no JAR 7.0.14 até que sejam integradas e validadas em servidor.

## Direção do desenvolvimento

O objetivo de longo prazo é transformar o Terra2 em uma plataforma de geração onde cada mundo possa definir independentemente terreno, biomas, estruturas, features e, futuramente, loot e comportamento de itens isolados por mundo, mantendo compatibilidade com o ecossistema de packs Terra.

Uma ferramenta separada, **Terra2Dev**, também está sendo considerada para criação, validação, migração e testes de packs sem aumentar desnecessariamente o runtime do servidor.

Terra2 é um projeto sucessor e não é uma versão oficial do projeto Terra original.
