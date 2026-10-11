# Biomas próprios do motor e equivalentes vanilla — Terra2 7.0.25-BETA

O modo `VANILLA_COMPATIBLE` mantém a identidade do pack dentro do Terra2, mas grava um bioma vanilla nos chunks para consultas Bukkit comuns. Faz parte do JAR do motor; não exige a ponte separada nem um Aeternum modificado.

## Primeiro teste: mundo novo

Acrescente uma entrada à sua configuração existente, preservando os outros mundos:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_compat_teste:
    pack: HYDRAXIA
    biomes:
      mode: VANILLA_COMPATIBLE
```

Após instalar a build e reiniciar, use `/terra2 reload` quando editar a lista de mundos. Crie o mundo com Multiverse:

```text
/mv create terra2_compat_teste normal --generator Terra2:HYDRAXIA
/mv tp terra2_compat_teste
/terra2 biome
```

`/terra2 biome` lê a identidade persistida do Terra2 e mostra também o bioma atual do Bukkit. Se o registro próprio estiver ausente ou inválido, informa `UNKNOWN`; não inventa o bioma com base no pack atual.

O modo normal continua sendo `CUSTOM`. Instalar a build não ativa a geração nem converte saves. Um mundo compatível não pode mudar de modo ou de referências em reload. Um manifesto vincula UUID, seed, modo, versão do formato e fingerprint do pack/referências; divergências no reinício são recusadas. Não edite ou apague o manifesto para contornar a validação.

O modo compatível é reservado a mundos novos. Nesta build, aceita uma base Community Pack, sem overlays convertidos ou terreno do adaptador vanilla. O modo `CUSTOM` continua suportando as composições existentes. As estruturas e decorações próprias dos estágios Terra permanecem; as features vanilla adicionais são desativadas para não duplicar árvores/minérios. Não anunciamos paridade de estruturas nativas vanilla nesse modo inicial.

## Referências por bioma

Por padrão, cada bioma usa a referência vanilla que o próprio pack já declara. Isso não é uma classificação automática baseada no nome. Se o autor escolheu um equivalente inadequado, configure uma substituição explícita:

```yaml
worlds:
  terra2_compat_teste:
    pack: HYDRAXIA
    biomes:
      mode: VANILLA_COMPATIBLE
      references:
        SNOWSWEPT_MEADOWS: minecraft:snowy_plains
```

Use o ID real mostrado pelo pack ou o ID completo `terra:...`. IDs desconhecidos, duas referências para o mesmo bioma ou um equivalente que não existe no registro vanilla são recusados. As referências devem considerar temperatura, precipitação, cores e fauna; não alteramos os YAML dos packs da comunidade.

## Persistência e leituras próprias

Os dados ficam no PDC do próprio chunk, em `terra2:biome_identity_v1`, e são gravados durante a geração nativa. Não há arquivo lateral por chunk, fila de gravação posterior nem reconstrução de chunks antigos.

O registro contém schema, UUID, seed, coordenadas do chunk, faixa de altura, pack, fingerprint, uma paleta de identidades e índices em células 4×4×4. Inclui CRC para detectar corrupção e limites de tamanho. O manifesto do mundo é independente do payload dos chunks e é gravado por renomeação atômica.

O motor continua consultando seu provedor original para terreno, paletas e estágios do pack. Para consultar áreas já geradas, o serviço público `WorldBiomeService` lê o registro salvo, sem depender do bioma Bukkit atual. O serviço não carrega chunks. Ausência/corrupção/UUID incompatível produz `Optional.empty()`, sem reescrever o save.

```java
WorldBiomeService service = Bukkit.getServicesManager().load(WorldBiomeService.class);
Optional<BiomeIdentity> identity = service == null
    ? Optional.empty() : service.at(world, x, y, z);
```

Chamadas devem ocorrer na thread responsável pelo mundo/chunk. Não sombreie as classes da API; use dependência de compilação e `softdepend: [Terra2]`.

## Compatibilidade e limites

Um plugin que consulta `World#getBiome()` recebe o equivalente vanilla sem usar nossa API. Isso resolve a classificação de biomas; não força o calendário do Aeternum, não impede que plugins externos reescrevam biomas e não garante compatibilidade com todos os seus sistemas.

O cliente também usa o equivalente vanilla, inclusive suas cores e clima. Personalizações visuais do bioma customizado não são mantidas por esse modo. Regras naturais de criaturas passam a usar o bioma vanilla, mas spawners, estruturas, comandos e outros plugins podem criar criaturas diferentes. Não adicionamos ainda um controlador de spawn do Terra2.

O fingerprint considera a versão declarada do pack e seu catálogo de biomas/referências; não detecta toda edição de arquivo feita sem alterar a versão. Preserve a versão do pack usado no mundo e mantenha backups do mundo completo, incluindo o manifesto e os chunks com PDC.

## Validação

A CI compila o motor, verifica o codec e executa dois boots de Paper 26.2 com HYDRAXIA. O teste compara terreno com um mundo de controle, confere referências vanilla, identidades próprias, coordenadas negativas, alturas, leitura sem carga de chunks, corrupção, reload recusado e preservação exata do payload após reinício. Resultado: **PASSOU** na [CI 38121349293](https://github.com/mrserluiz/Terra2.0/actions/runs/38121349293). Os dois boots preservaram a identidade `terra:hydraxia/hydraxia/drafty_streams`, enquanto Bukkit devolveu `minecraft:frozen_river`; 1.368 amostras de blocos coincidiram com o controle.

[Download do motor 7.0.25-BETA](https://github.com/mrserluiz/Terra2.0/actions/runs/38121349293/artifacts/11695343432): o ZIP contém **um único JAR**, `Terra2-bukkit-7.0.25-BETA.jar`. Substitua o JAR anterior do Terra2 e reinicie para atualizar o código. Não instale outra versão simultaneamente. Configure um nome de mundo novo para o teste. [Relatório e limites da build](testing/BIOME_COMPATIBILITY_7.0.25.md).

SHA-256 do JAR: `9d937569643df2578e958da6cd443eeaf658941b15fba1e9712255f47d2e4979`.
