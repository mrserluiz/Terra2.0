# Paleta visual genérica Bedrock — 7.0.27-BETA

O Geyser oficial deve ser mantido. A build 7.0.26 usou a proposta experimental PR #6658 para validar cores de biomas; aquele Geyser não é requisito nem a solução escolhida para distribuição. A nova build não compila outro Geyser e `/terra2 bedrock install` recusa a instalação experimental.

## Estado real

Esta etapa implementa o compilador de um pack de recursos RGB independente do gerador. **Não aplica cores aos chunks ou à tela do jogador.** A seleção de variantes por posição está `BLOCKED_PENDING_POSITION_TRANSPORT`. Um resource pack sozinho não transmite a identidade Terra2, nem executa behavior packs num servidor Paper.

## Gerar a paleta

Como OP, execute:

```text
/terra2 bedrock pack
/terra2 bedrock status
```

Saída: `plugins/Terra2/bedrock/terra2-palette.mcpack`. Dentro do arquivo há:

- `manifest.json`: identidade do pack calculada a partir do conteúdo para invalidar o cache quando a paleta muda.
- `terra2-palette.json`: perfis de origem, cores RGB exatas, nomes estáveis das texturas e estado da futura projeção.
- `textures/terrain_texture.json`: entradas próprias do atlas `terra2_rgb_<rrggbb>`.
- `textures/terra2/palette/*.png`: amostras opacas de cor, 16×16, geradas pelo Terra2. Não são as texturas de grama/folhas do Minecraft e não alteram texturas vanilla.

A base contém 256 cores RGB332. Cada RGB solicitado que ainda não exista é adicionado **exatamente**, sem aproximar ao RGB332. Por exemplo, `#FFD1EF` continua `#FFD1EF`. O espaço RGB aceita 16.777.216 cores, mas o pack contém somente a base e as cores solicitadas. O limite desta etapa é 4096 cores por catálogo; excesso é recusado antes de sobrescrever a saída. Não há promessa de registrar milhares de permutations de blocos: o transporte ainda será definido.

Copiar este pack para `plugins/Geyser-Spigot/packs` permite preparar o envio do atlas, mas **não ativa as variantes no mundo**. Não é necessário instalá-lo no servidor de produção nesta etapa. Não há mappings globais substituindo `minecraft:grass_block`, folhas ou biomas. Não altera terreno, loot, biomas ou dimensões. Não precisa regenerar mundos.

O compilador compartilha uma única amostra por RGB entre todos os perfis e detecta IDs conflitantes. Repetir o comando preserva bytes/UUIDs quando o catálogo não muda. Saídas não identificadas como nossa paleta são recusadas; os packs e mappings de terceiros não são mesclados/sobrescritos.

## API para outros geradores

No JAR Terra2:

```java
org.terra2.api.visual.VisualProfile
org.terra2.api.visual.VisualPaletteProvider
```

`VisualProfile` contém um ID namespaced e um mapa de cores RGB de 24 bits. Papéis suportados: `grass`, `foliage`, `dry_foliage`, `water`, `sky`, `fog`, `water_fog`. Cada valor é um inteiro entre 0 e `0xFFFFFF`.

Um plugin externo registra `VisualPaletteProvider` no Bukkit ServicesManager com `apiVersion() == 1`. `profiles()` fornece o catálogo. O comando de compilação reúne todos os providers registrados e as cores dos Community Packs realmente registrados pelo Terra2. Exemplo:

```java
public Collection<VisualProfile> profiles() {
    return List.of(new VisualProfile("meugerador:planicie_congelante",
        Map.of("grass", 0x80D9FF, "foliage", 0xBBDFFF, "water", 0x77AACC)));
}
```

A API é de **catálogo**, sem seleção por coordenadas nesta etapa. Deve ser implementada numa integração externa; registrar o provider não adapta automaticamente qualquer gerador. O provider deve fornecer um snapshot pequeno, sem carregar chunks ou executar geração. O comando é administrativo, executado na thread do servidor; não existe scanner contínuo.

## Próxima etapa

Usar a identidade persistente Terra2 para resolver perfis por posição e validar um transporte de variantes para o Geyser oficial, incluindo chunks iniciais, atualizações de blocos, reconexão e troca de mundo. Preservar aparência vanilla fora dos mundos autorizados. Não usar overrides globais nem estados de blocos reservados por outros plugins como atalho.

Água, céu e névoa são pedidos de cor no catálogo; não se tornam automaticamente variantes de blocos sólidos. Precisam de tratamento específico. A API pública de névoa por jogador é um caminho separado a explorar. A compatibilidade Java/Bedrock e a renderização em cliente real continuam pendentes.

## Validação

Testes de unidade verificam RGB exato nos pixels, deduplicação entre geradores, determinismo, conflitos, limites e preservação de arquivos não pertencentes ao Terra2. O teste Paper gera a paleta pelo comando e verifica repetibilidade, reinício e ausência de alterações no bioma principal. A validação do motor passou na [execução 38163982449](https://github.com/mrserluiz/Terra2.0/actions/runs/38163982449), fonte `f72d60425e75cfd44651e3f69c37b777feb99a24`. Em Paper 26.2, o comando gerou 484 cores exatas para 328 perfis, pack de 148.937 bytes. Inicialização e reinício emitiram `TERRA2_GENERIC_PALETTE_OK`; arquivo e UUIDs permaneceram idênticos. Não houve Geyser modificado nem cliente Bedrock nessa validação.

Artefatos da execução: `Terra2-engine-experimental-26.2` contém o JAR 7.0.27; `Terra2-native-integration-diagnostics` contém o pack e logs em `biome-smoke`. Não use o artefato `Terra2-plugin` como motor independente.

SHA-256 do JAR: `9a4ece29f3272dbf84946171639f7bd1dafba81867c905af1c13023c6518fe6a`.

SHA-256 do pack: `d67b7cd8f796567ef6b3b27b5f8e4d4481f033ca2aaa9ddfe3bd260dc7322c2d`.
