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

## Testes

```bash
mvn test
```

Requer Java 17 ou superior.
