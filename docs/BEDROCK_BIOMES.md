# Cores de biomas no Bedrock — Terra2 7.0.26-BETA

Integração experimental integrada ao JAR do Terra2. O gerador e os blocos não são modificados por estes comandos.

## O que faz

Lê os biomas Community Pack realmente registrados no adaptador Paper 26.2, incluindo os valores herdados do bioma base. Exporta um mapping Geyser com os IDs canônicos `terra:<pack>/<namespace>/<bioma>`: água, overrides de grama, folhagem e folhagem seca, céu, névoa e névoa aquática. Sem override de vegetação, o Geyser usa a temperatura/umidade recebida do registro Java. Modificadores espaciais de grama, partículas, sons e efeitos dependentes de horário não são reproduzidos integralmente nesta etapa.

Não cria uma textura global nem substitui `minecraft:*`: as cores acompanham cada ID de bioma. O Geyser gera e envia seu próprio `custom_biomes.mcpack`. Outros packs de itens/blocos continuam separados e não são mesclados/sobrescritos pelo Terra2.

## Dependência real e estado experimental

O Geyser oficial consultado em 11/10/2026 ainda não contém a API. A implementação usada no teste é a proposta [Geyser PR #6658](https://github.com/GeyserMC/Geyser/pull/6658), de darkcupid412, commit `c609c0e517954fbb58579c24c81564724a3be3bb`. Não é uma release oficial nem código criado pelo Terra2. CI compila essa fonte sem alterar seu motor e testa o mapping produzido pelo nosso registro real. A proposta tem autoria/licença próprias, preservadas no JAR; não redistribuímos o nome como uma versão oficial.

A versão atual instalada pode permanecer: exportar funciona sem Geyser, e a instalação recusa versões sem as classes de API/leitor/gerador de pack. Detecção de classes confirma capacidade disponível, não confirma que o cliente aceitou o pack ou que custom content foi habilitado.

## Comandos (OP / terra2.settings.reload)

```text
/terra2 bedrock status
/terra2 bedrock export
/terra2 bedrock install
```

- `status`: informa GEYSER_ABSENT, UNSUPPORTED_CUSTOM_BIOMES ou CUSTOM_BIOME_API_PRESENT.
- `export`: grava `plugins/Terra2/bedrock/terra2-biomes.json` com todos os biomas Community Pack carregados; não depende de mundos ativos nem carrega chunks.
- `install`: exige Geyser-Spigot habilitado e com suporte; exporta e instala SOMENTE `plugins/Geyser-Spigot/custom_mappings/terra2-biomes.json`. Se esse arquivo já existir sem a marca do exportador, a substituição é recusada. Os demais mappings não são tocados.

Os mappings são registrados na inicialização do Geyser, portanto reinicie o servidor após instalar/atualizar. Jogadores devem aceitar o pack visual e reconectar. Não use PlugMan para trocar o Geyser nem regenere mundos para aplicar cores. Se o Geyser estiver em proxy/standalone, use export e copie o JSON para `custom_mappings` dessa instância; install só cobre Spigot local.

## Primeiro teste

1. Faça o teste numa cópia do servidor; mantenha seu JAR atual do Geyser para retorno.
2. Substitua o Terra2 pela build 7.0.26-BETA e, só na cópia, o Geyser pelo artefato explicitamente experimental PR6658. Mantenha Floodgate e os packs existentes.
3. Inicie, execute status e install, reinicie.
4. Confira no console o registro dos mappings e a geração do pack. Entre com um cliente Bedrock compatível com essa build, aceite o download e compare água/grama/folhas ao Java na mesma posição. Teste HYDRAXIA e uma região vanilla para verificar ausência de recoloração global. Confira a versão Bedrock suportada na mensagem do próprio Geyser; a proposta está baseada na linha feature/26.50, e não garante todo cliente antigo.
5. Se houver falha de login/pack, volte ao Geyser anterior com o servidor desligado e retire apenas `custom_mappings/terra2-biomes.json`.

## Identidade nos chunks e AeternumSeasons

Este caminho depende dos IDs personalizados Java presentes nos chunks. Funciona com `biomes.mode: CUSTOM` (padrão), sem converter chunks.

`VANILLA_COMPATIBLE` escreve IDs vanilla para a compatibilidade Bukkit; o Geyser não recebe a identidade paralela do PDC. Nesta primeira etapa não há recuperação por pacote/rewrite do PDC para o Geyser. NÃO mude um mundo existente para CUSTOM/VANILLA_COMPATIBLE para testar: a política persistente impede mistura de modos; use um mundo novo caso precise testar o outro modo.

Se AeternumSeasons ou outro plugin chamar `World#setBiome` e gravar snowy_plains no chunk, o cliente recebe snowy_plains, e o mapping de Sakura deixa de ser usado. O comando `/terra2 biome` em CUSTOM pode prever Sakura pelo provider, mas isso não prova que o ID entregue ao cliente ainda seja Sakura. Corrigir cores nessas condições exigirá uma futura projeção visual por sessão; esta build não altera o AeternumSeasons do autor nem promete resolver essa etapa.

## Limites de validação

CI verifica exportação real, leitura no código Geyser e cores dos assets gerados. Isso não é uma conexão com cliente Bedrock real. A aceitação do pack, o rendering em celular/console e coexistência com os plugins EtherCraft precisam do teste do usuário. Vibrant Visuals pode reinterpretar água/céu/névoa; o autor da proposta relata maior fidelidade no renderizador clássico. Nenhuma garantia de igualdade pixel a pixel entre edições.

A geração de terreno continua Paper 26.2. O trabalho não moderniza automaticamente o motor para 26.3. A falha separada de NBT do REIMAGEND ainda existe.
