# Terra 2.0 — núcleo inicial

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

O núcleo atual apenas toma decisões de segurança. A integração Paper/Terra e a
criação efetiva de mundos serão adicionadas depois que este contrato estiver
estável.

## Integração de packs e mundos

O segundo módulo adiciona:

- descoberta de Community Packs em pasta ou `.zip`;
- leitura de `id`, `version` e addons obrigatórios do `pack.yml`;
- validação dos addons disponíveis no runtime do Terra;
- orquestração da criação por uma interface isolada do Paper;
- manifesto `.terra2/manifest.properties` dentro de cada mundo gerenciado;
- bloqueio anterior à chamada do gerador quando o mundo não é autorizado.

O adaptador Paper chamará o Terra somente após todas essas verificações. O ponto
de integração confirmado para a versão investigada é
`TerraBukkitPlugin#getDefaultWorldGenerator(worldName, packId)`.

## Testes

```bash
mvn test
```

Requer Java 17 ou superior.
