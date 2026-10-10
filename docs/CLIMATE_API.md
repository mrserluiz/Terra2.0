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

Instale o motor Terra2 e, opcionalmente, `Terra2AeternumBridge-1.0.1-BETA.jar`. **Mantenha o AeternumSeasons oficial, publicado por seu autor. Nenhuma substituição por um fork é exigida ou recomendada.** Troca de JAR requer reinício.

A ponte detecta capacidades, sem fixar uma versão do consumidor. Procura os métodos públicos `registerWorldClimateProvider(Plugin, Function)` e `unregisterWorldClimateProvider(Plugin)`, ambos retornando `void`, no plugin ou no objeto retornado pelo accessor configurado. Esse é um contrato opcional da ponte, **não uma API oficial presumida do Aeternum**. Nome/versão do plugin não comprovam compatibilidade.

Em `plugins/Terra2AeternumBridge/config.yml`:

```yaml
receiver:
  plugin: AeternumSeasons
  accessor: getSeasons
```

Outros consumidores com o mesmo contrato podem ser selecionados. A detecção acompanha ativação/desativação do consumidor e registro/remoção do serviço Terra2. Não usa campos privados, não injeta classes, não executa comandos globais de estação e não altera configurações, backups ou biomas.

Controle (OP por padrão): `/terra2climatebridge status`, `disconnect` e `connect`. O status inclui o motivo da inatividade e a versão detectada. Sem receptor compatível, informa `UNSUPPORTED` uma vez por mudança de estado e deixa o Aeternum funcionar normalmente. **Nesse estado, a estação e a referência de neve do Terra2 não são aplicadas ao Aeternum.** A ponte não corrige o tratamento de biomas customizados dentro do plugin oficial.

Não precisamos solicitar mudanças ao autor: o serviço público Terra2 continua disponível para consumidores independentes, e adaptadores adicionais podem usar interfaces existentes quando verificadas. A compatibilidade só será anunciada para interfaces efetivamente validadas.

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

## Testes e limites

Os testes de descoberta verificam receptores públicos no plugin/objeto interno e rejeitam consumidores sem contrato. A CI inspeciona o JAR oficial 4.7, fixado ao commit `c0cfd8b7c0093c9b7ff15d8112a1c26b7be584ed`, para registrar assinaturas públicas e capacidades. Essa inspeção não equivale a um teste de clima no servidor oficial.

O teste Paper/HYDRAXIA anterior usa um **fork experimental como fixture de receptor**, e valida o protocolo, reload, biomas preservados, salvamento/reinício e remoção da ponte. Seus resultados não comprovam compatibilidade do Aeternum oficial. O fork não integra o download normal da ponte. As notas históricas estão em [CLIMATE_INTEGRATION_7.0.24.md](testing/CLIMATE_INTEGRATION_7.0.24.md).

### Resultado da ponte 1.0.1

[CI 38085545220](https://github.com/mrserluiz/Terra2.0/actions/runs/38085545220): compilação, 3 testes de descoberta e teste Paper com receptor experimental aprovados. No JAR **oficial 4.7**, SHA-256 `0a3c9f1ecfa85343fd96c00002fb88e987510f7ab9b66c95e329cb25eb4677f2`, os métodos de registro/remoção do contrato não foram encontrados. Essa versão não recebe o override por esta ponte. Os métodos públicos de estação encontrados operam por calendário compartilhado; não foram usados como substituto de um perfil isolado por mundo.

[Download da ponte isolada](https://github.com/mrserluiz/Terra2.0/actions/runs/38085545220/artifacts/11682820702). SHA-256 do JAR 1.0.1: `527b1dab1407b7488998987bb7470e191c1c752a48a2792699bb3a511030c3ce`. Não inclui nem exige um Aeternum modificado.
