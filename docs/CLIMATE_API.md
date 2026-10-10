# Terra2 climate metadata API / API de clima por mundo

**Terra2 fornece os dados. O plugin de clima os consome.** O gerador não depende do AeternumSeasons, e esta API não altera biomas nem força tempestades.

## Configuração no Terra2

Em `plugins/Terra2/terra2-settings.yml`, acrescente o perfil somente ao mundo que já usa nosso gerador:

```yaml
climate:
  enabled: true
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  aeternum_frost:
    pack: HYDRAXIA
    climate:
      season: WINTER
      reference-biome: minecraft:snowy_plains
```

Mantenha suas outras entradas de mundos. Use o nome exato e o ID do pack. `/terra2 reload` valida e atualiza os metadados sem reiniciar os plugins. Perfis inválidos preservam o estado anterior. A instalação começa com `climate.enabled: false`, geração desativada e nenhum mundo selecionado.

Somente mundos autorizados, carregados com o gerador Terra2, recebem metadados. O mundo principal e os mundos protegidos nunca recebem perfil desta API. Mesmo que um mundo tenha entrada YAML, um gerador diferente não recebe perfil.

Não é necessário recriar um mundo que já usa Terra2. A configuração não cria mundos e não substitui o gerador de saves existentes.

## Addon opcional para AeternumSeasons

1. Instale **Terra2-bukkit-7.0.24-BETA.jar** (substitui o gerador Terra2 anterior).
2. Use **AeternumSeasons-4.5.2-CLIMATE-API-BETA.jar** ou uma versão que ofereça a API genérica `registerWorldClimateProvider`. Substitua apenas o JAR antigo do Aeternum, preservando seus arquivos.
3. Instale **Terra2AeternumBridge-1.0.0-BETA.jar** para conectar os dois. Reinicie após trocar/adicionar JARs.

O addon é outro plugin, não outro gerador. Nenhum dos plugins principais depende dele. Não instale simultaneamente duas versões do Terra2 ou do Aeternum.

A ponte lê o serviço publicado pelo Terra2 e fornece ao Aeternum somente o perfil do mundo solicitado. Não copia YAML, não escreve biomas, não altera JARs e não exporta configurações do Aeternum para o Terra2. Sem um dos plugins, a ponte fica inativa. Sem a ponte, os dois funcionam separadamente. Aeternum antigo sem API de consumo não é modificado por reflexão: a ponte fica inativa e informa a incompatibilidade uma vez.

O Aeternum usa o perfil como referência de temperatura, estação e classificação de neve. Preserva os IDs reais dos biomas, bloqueando o spoof/restauro sazonal naquele mundo. Isso não transforma visualmente chuva em neve em um bioma quente nem converte regras de agricultura/fauna.

Se você instalou a beta 4.5.1 anterior, remova ou desative apenas o perfil local criado em `AeternumSeasons/climate.yml` para o mesmo mundo. Os perfis agora pertencem ao Terra2. Perfis locais escritos pelo usuário permanecem suportados pelo Aeternum; os metadados externos têm prioridade enquanto a ponte estiver disponível.

## API pública para outros plugins

O contrato está em `org.terra2.api.climate`. Não é específico do Aeternum:

```java
WorldClimateService climate = Bukkit.getServicesManager().load(WorldClimateService.class);
if (climate != null && climate.apiVersion() == 1) {
    Optional<WorldClimateProfile> profile = climate.profile(world);
    // season() and referenceBiome() are metadata, not biome mutations.
}
```

Um consumidor pode compilar contra o JAR do Terra2 como `compileOnly` (não sombrear essas classes), declarar `softdepend: [Terra2]`, observar registro/remoção de serviços e usar seu comportamento normal na ausência do provedor.

Para adaptadores sem ligação de classes, `describe(World)` fornece um mapa imutável: `schema`, `source`, `world`, `world-uuid`, `dimension`, `season`, `reference-biome`. Mapa vazio significa nenhum override. Schema atual: `1`. O contrato usa nomes de mundo exatos e UUIDs para evitar aplicar dados a outro save.

Os metadados não incluem loot, drops, texturas ou regras de itens. Esses sistemas continuam separados.

## Testes

CI verifica a geração Terra2 independente, depois Terra2 + ponte + Aeternum em um servidor Paper 26.2 isolado usando HYDRAXIA. A prova inclui bioma customizado preservado, reload válido/inválido, mundo principal protegido, desconexão/reconexão da ponte, salvamento/reinício e inicialização sem addon. Logs e JARs são publicados em artifacts separados para distinguir o motor da integração opcional.
