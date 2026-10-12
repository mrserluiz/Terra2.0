# Paleta visual genérica Bedrock — 7.0.29-BETA

O Geyser oficial deve ser mantido. A build 7.0.26 usou a proposta experimental PR #6658 para validar cores de biomas; aquele Geyser não é requisito nem a solução escolhida para distribuição. A nova build não compila outro Geyser e `/terra2 bedrock install` recusa a instalação experimental.

## Estado real

Esta etapa implementa o compilador de um pack de recursos RGB independente do gerador. A paleta não modifica chunks salvos. A seleção por posição agora possui uma **prévia opt-in de grama**, descrita abaixo. Água, folhas, céu e aplicação completa em chunks ainda não estão implementados. Um resource pack sozinho não transmite a identidade Terra2, nem executa behavior packs num servidor Paper.

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

A API é de **catálogo**, sem seleção por coordenadas nesta etapa. Deve ser implementada numa integração externa; registrar o provider não adapta automaticamente qualquer gerador. O provider deve fornecer um snapshot pequeno, sem carregar chunks ou executar geração. A compilação é administrativa, executada na thread do servidor. Apenas jogadores em prévia explícita têm uma busca local limitada, uma vez por segundo.

## Próxima etapa após a prévia

Substituir a prévia local por um transporte completo de variantes, incluindo chunks iniciais, atualizações de blocos, reconexão e troca de mundo. Preservar aparência vanilla fora dos mundos autorizados. Não usar overrides globais nem estados de blocos reservados por outros plugins como atalho.

Água, céu e névoa são pedidos de cor no catálogo; não se tornam automaticamente variantes de blocos sólidos. Precisam de tratamento específico. A API pública de névoa por jogador é um caminho separado a explorar. A compatibilidade Java/Bedrock e a renderização em cliente real continuam pendentes.

## Validação

Testes de unidade verificam RGB exato nos pixels, deduplicação entre geradores, determinismo, conflitos, limites e preservação de arquivos não pertencentes ao Terra2. O teste Paper gera a paleta pelo comando e verifica repetibilidade, reinício e ausência de alterações no bioma principal. A validação do motor passou na [execução 38163982449](https://github.com/mrserluiz/Terra2.0/actions/runs/38163982449), fonte `f72d60425e75cfd44651e3f69c37b777feb99a24`. Em Paper 26.2, o comando gerou 484 cores exatas para 328 perfis, pack de 148.937 bytes. Inicialização e reinício emitiram `TERRA2_GENERIC_PALETTE_OK`; arquivo e UUIDs permaneceram idênticos. Não houve Geyser modificado nem cliente Bedrock nessa validação.

Artefatos da execução: `Terra2-engine-experimental-26.2` contém o JAR 7.0.27; `Terra2-native-integration-diagnostics` contém o pack e logs em `biome-smoke`. Não use o artefato `Terra2-plugin` como motor independente.

SHA-256 do JAR: `9a4ece29f3272dbf84946171639f7bd1dafba81867c905af1c13023c6518fe6a`.

SHA-256 do pack: `d67b7cd8f796567ef6b3b27b5f8e4d4481f033ca2aaa9ddfe3bd260dc7322c2d`.

## Prévia por posição — 7.0.28-BETA

**Geyser oficial local (`Geyser-Spigot`)**, Paper 26.2, custom content habilitado. Sem fork, sem addon externo e sem dependência obrigatória. Geyser em proxy/standalone e Folia não são atendidos por esta prévia.

1. Troque apenas o JAR Terra2 pela build 7.0.28 e inicie normalmente.
2. Como OP, execute `/terra2 bedrock prepare`. Isso grava uma autorização própria em `plugins/Terra2/bedrock/preview.enabled`; não modifica o YAML nem cria mundos.
3. Reinicie. Antes de aceitar logins, o Terra2 prepara a paleta e a registra pelo evento público de packs do Geyser. Registra até 128 blocos de cor próprios pelo evento público de blocos, **sem overrides Java ou de itens**. O Geyser oferece o pack durante o login. Não precisa copiar o pack para sua pasta nem instalar outro Geyser. Para o teste, aceite o pack ao entrar; recuse a prévia se o cliente não o carregar.
4. Confira `/terra2 bedrock status`: deve mostrar `READY_OPT_IN`. Se apresentar `FAILED_DISABLED`, procure a causa no console e em `plugins/Terra2/reports`; o motor de geração não é desativado.
5. Entre pelo Bedrock em um mundo Terra2 autorizado com grama próxima e execute `/terra2 bedrock preview`. A prévia colore até 64 blocos de grama por jogador, num raio horizontal de 4 e faixa vertical de ±3. Limite de dois participantes. É uma amostra de cor sólida em cubos, **não a textura final de grama com terra nas laterais**. Sem folhas, água, céu ou névoa nesta etapa.
6. Use status para ver a quantidade de updates submetidos. Submeter pacotes não prova que o cliente renderizou. Compare o mesmo local pelo Java: seus blocos/biomas ficam inalterados.
7. Execute `/terra2 bedrock stop` para restaurar a aparência enviada pelos mappings normais. Teleporte, mudança de mundo, perda da permissão OP e desconexão encerram a prévia; reative por comando quando retornar/reconectar.
8. `/terra2 bedrock disable` encerra as prévias e retira a autorização persistente. Reinicie para retirar o pack/registros desta sessão. Não use PlugMan nem `/geyser reload` durante o teste.

### Leitura e transporte

Em `VANILLA_COMPATIBLE`, lê a identidade original persistente de cada posição. Se estiver ausente/inválida, não reconstrói nem aplica cor. Em CUSTOM, que não guarda a paleta PDC, usa uma **previsão do provider vinculado ao mundo**; não afirma que ela é um dado histórico salvo do chunk. Biomas Bukkit alterados por outro plugin não determinam essa seleção. Somente GRASS_BLOCK existente, chunks já carregados e mundos autorizados entram na busca. Sem geração adicional por lookup e sem alterações nos blocos salvos.

A API pública registra packs e blocos, mas o envio arbitrário por posição usa um adaptador isolado às classes internas de sessão/protocolo do Geyser oficial, acessadas por reflexão. **Não é uma API pública estável**. Verifica métodos, runtime IDs e definições; incompatibilidade desativa apenas a prévia, com um reporte. Nenhum ID de protocolo é fixado manualmente. Atualizações futuras do Geyser podem exigir ajustes nesse adaptador.

O envio ocorre no event loop da sessão, com tokens que invalidam lotes após stop/teleporte. Updates normais do Geyser ou reenvios de chunks podem substituir a prévia entre os refreshes de um segundo; essa limitação será tratada antes de habilitar projeção permanente. Não há mudança automática para todos os jogadores.

### Validação da nova build

CI baixa um binário **oficial**, verifica o SHA-256 divulgado pela API de downloads e registra a versão/build usada no diagnóstico. Testa registro de pack e blocos, construção de updates com runtime IDs reais, captura isolada do envio, rejeição de lotes expirados, lookup da identidade persistente, proteção do mundo principal e reinício. Não conecta um cliente Bedrock real nem simula renderização. O teste visual final é o procedimento acima.

Resultado da build 7.0.28: [CI 38167748169](https://github.com/mrserluiz/Terra2.0/actions/runs/38167748169), fonte `9a33260530c5e4fffab9a1e3bbbd4649c96c4549`. Motor, persistência/reinício e ponte oficial passaram. Geyser oficial 2.11.3 build 1251, Paper 26.2: 65 cores de grama e 195 definições de protocolo validadas em ambas as inicializações. Captura de envio isolada; cliente Bedrock real não conectado.

SHA-256 do JAR `Terra2-bukkit-7.0.28-BETA.jar`: `d8edae81662388ae1a4bdcffb434d96758086ff044b691b88025b826d235fc7d`. Pack da fixture HYDRAXIA: `525e113db5d610a51e49c6a3cf6139a9850cb21e049649d0bb32210fd17f44a4`. O pack oferecido no servidor é recompilado a partir do catálogo local e pode ter outro hash.

## Biomas visuais nativos — 7.0.29-BETA experimental

`/terra2 bedrock native` inicia uma rota separada da prévia de cubos. Requer Geyser-Spigot oficial local, pack aceito, OP, mundo autorizado com armazenamento CUSTOM e IDs Terra realmente presentes na conexão. Não muda o modo de um mundo existente.

O pack contém definições `minecraft:client_biome` com namespace `terra2:` e cores exatas de água, grama e folhagem. Usa as texturas vanilla do cliente: água mantém sua animação, níveis e geometria de líquido; folhas mantêm as máscaras e transparência nativas. Plantas que não usam tint de bioma ou possuem cores fixas não são recoloridas. Nenhum PNG Mojang é redistribuído: Flora.zip não precisa ser instalado para esta rota.

O adaptador clona definições vanilla do Geyser sem editar seus registros globais. Aloca IDs separados a partir de 30000, preserva os dados ambientais da referência e o alpha da cor de mapa de água. Não força `surface_opacity`. Envia um BiomeDefinitionListPacket e altera somente as traduções Terra do cache daquela conexão, em seu event loop. Traduções minecraft e blocos de água não são alterados. Usa classes internas do Geyser e pode exigir atualização de compatibilidade.

**Limitação experimental:** a definição é enviada depois do login, quando o operador inicia o teste. A aceitação de novos biomas nessa fase depende do cliente e ainda precisa de validação Bedrock real. `NATIVE_INSTALLED_EXPERIMENTAL` comprova somente execução do transporte; não comprova renderização. O adaptador não reenvia chunks já vistos. Caminhe até chunks ainda não recebidos nessa conexão, sem teleportar após o comando. O teste não impõe limite de chunks à exploração normal do jogador.

`/terra2 bedrock status` informa pendência, instalação ou falha. `/terra2 bedrock stop`, teleporte, troca de mundo, perda de permissão e saída interrompem o transporte e restauram as entradas ainda pertencentes ao adaptador. Biomas dos chunks que o cliente já recebeu podem continuar visuais até reconectar. Reconfigure/reconecte também após atualizar o pack. A rota não é ativada para jogadores comuns automaticamente.

Se outro plugin substituir um bioma Terra por `minecraft:snowy_plains` ou outro ID vanilla antes de enviar o chunk, essa identidade não pode ser recuperada pelo cache de tradução. O pack não redefine snowy_plains globalmente. Mundos VANILLA_COMPATIBLE continuam recusados pelo protótipo; a futura projeção de chunks a partir do leitor persistente é necessária para esse caso.

Teste com água parada e corrente, imersão, bordas entre biomas, folhas e grama; comparar Java no mesmo local. Após stop/reconectar, conferir o mundo principal. Reportar status e log; nenhuma confirmação visual deve ser inferida apenas da CI.

Referências: [client biomes](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/clientbiomesreference/examples/clientbiomesoverview?view=minecraft-bedrock-stable), [water appearance](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/clientbiomesreference/examples/components/minecraftclientbiomes_water_appearance?view=minecraft-bedrock-stable).

### Resultado da validação 7.0.29

Motor e testes de geração/persistência/reinício: [run 38322434790](https://github.com/mrserluiz/Terra2.0/actions/runs/38322434790), fonte do JAR `8564e496282d3f985fabec3a41b3257775b86da3`. Teste corrigido e verificação Geyser: [run 38323135750](https://github.com/mrserluiz/Terra2.0/actions/runs/38323135750), fonte `696cdc2da9f9b016b890c0bd41f13197786ebda5`. A CI comparou todo o código do motor entre as duas revisões (excluindo ferramentas de teste) e confirmou igualdade antes de reutilizar o artefato.

Geyser oficial 2.11.3 build 1251 + Paper 26.2: passou em primeira inicialização e reinício, codificação nos codecs reais, cor de água 9B42FF, alpha herdado, registros globais preservados, cache de sessão isolado, restauração e preservação de uma alteração posterior de outra integração. Também validou todos os biomas do catálogo HYDRAXIA do mundo CUSTOM e a recusa de VANILLA_COMPATIBLE. O pack da fixture contém 328 definições de client biome. Sem cliente Bedrock real: aceitação tardia dos biomas e renderização continuam experimentais.

JAR SHA-256: `cf6501e3f23b20476e81677d6cd201b12d065f559e5877df11a652aa5549e04e`. Pack da fixture SHA-256: `5ad37ee304f6ca40588d9e840cc00b079f60c119eed1e2deb47fdc8dd03848cf`. O catálogo local do operador determina o pack que será oferecido no servidor.
