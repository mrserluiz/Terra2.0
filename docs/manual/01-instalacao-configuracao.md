# Instalação e configuração

1. Pare completamente o servidor Paper compatível com a build escolhida.
2. Instale o JAR Terra2 na pasta `plugins/` (remova versões duplicadas). Consulte as exigências de Java/Paper da sua release.
3. Inicie o servidor uma vez e confira os logs; não presuma que o plugin ativou a geração.
4. Edite **o arquivo de configurações do Terra2 gerado pela sua build** responsável por `generation` e `worlds` (não confunda com o `config.yml` legado do motor Terra).
5. Autorize apenas um mundo novo de teste, como no exemplo, preservando as demais configurações:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```

6. Execute `/terra2 reload` após alterar autorizações; se trocar o JAR, pare e reinicie completamente.
7. Crie o mundo com um gerenciador externo (ex.: Multiverse-Core). Terra2 não cria mundos automaticamente.

**Importante:** a configuração acima é um trecho ilustrativo do esquema documentado, não um arquivo completo pronto para substituir a configuração existente. O ID do pack precisa estar carregado. Mundos existentes não são regenerados; mudanças de pack, seed, dimensão ou limites de altura podem ser rejeitadas pelos manifests/fingerprints.

[Voltar ao índice](README.md)
