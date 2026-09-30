# Terra 2.0 — plugin Paper/Terra

Este módulo inicia a implementação do Terra 2.0 sem alterar o dump e os artefatos
usados na engenharia reversa.

## Princípio de segurança

O Terra 2.0 é **opt-in**:

- inicia com a geração desativada;
- bloqueia os mundos principais por padrão;
- não assume controle de mundos existentes não gerenciados;
- só autoriza mundos declarados explicitamente;
- exige um manifesto para voltar a carregar um mundo já criado pelo Terra 2.0;
- não regenera chunks existentes.

O plugin pode administrar vários mundos, mas a configuração distribuída contém
`generation.enabled: false` e `worlds: {}`. Portanto, a primeira inicialização é
inerte: nenhum mundo é criado, carregado ou modificado até o administrador
autorizar cada destino e seu pack explicitamente.

## Integração de packs e mundos

O segundo módulo adiciona:

- descoberta de Community Packs em pasta, `.zip` ou `.terra`;
- leitura de `id`, `version` e addons obrigatórios do `pack.yml`;
- validação dos addons disponíveis no runtime do Terra;
- orquestração da criação por uma interface isolada do Paper;
- manifesto `.terra2/manifest.properties` dentro de cada mundo gerenciado;
- bloqueio anterior à chamada do gerador quando o mundo não é autorizado.

O adaptador Paper chama o Terra somente após todas essas verificações, obtém o
gerador por `TerraBukkitPlugin#getDefaultWorldGenerator(worldName, packId)` e
então cria o mundo com `WorldCreator`. Cada entrada é processada isoladamente:
autorizar um mundo nunca autoriza os demais.

## Compilação

O alvo oficial permanece **Paper 26.2**, com `api-version: '26.2'`. O mesmo
código também é compilado e testado no CI contra a API do **Paper 26.3**, sem
transformar a versão nova na distribuição oficial antes do teste em servidor.

As duas versões requerem Java 25. O artefato oficial continua sendo produzido
com a API 26.2 e fica em `target/Terra2-0.1.0-SNAPSHOT.jar`. A verificação 26.3
não publica um segundo JAR; ela funciona como alerta antecipado de
incompatibilidade.

Para executar localmente a verificação da API 26.3:

```bash
mvn '-Dpaper.version=[26.3.build,26.4.build)' verify
```

## Testes

```bash
mvn test
```

Requer JDK 25 e Maven.
