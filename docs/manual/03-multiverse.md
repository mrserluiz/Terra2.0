# Criando mundos com Multiverse-Core

Terra2 é um **gerador**, não um gerenciador de mundos. O exemplo principal é o Multiverse-Core.

## Um Community Pack por mundo

Primeiro configure a autorização do mundo:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```

Depois, com o pack carregado:

```text
/terra2 reload
/mv create terra2_teste normal -g Terra2
/mv tp terra2_teste
```

A documentação técnica também admite a seleção explícita `--generator Terra2:OVERWORLD` para packs legados individuais. Use o método compatível com a build instalada e confirme a autorização antes de criar.

## Composição de packs

Somente uma base de terreno é permitida; outros packs devem ser extensões aditivas **suportadas e READY**:

```yaml
worlds:
  terra2_mix_teste:
    packs: [OVERWORLD, Debris]
```

```text
/terra2 reload
/mv create terra2_mix_teste normal --generator Terra2:PACKS
/mv tp terra2_mix_teste
```

Não confunda aceitar uma lista de IDs com conseguir executar qualquer datapack. Packs `BLOCKED` impedem a vinculação.

## Cuidados

- Nunca teste sobre `world`, `world_nether` ou `world_the_end`.
- Escolha nomes novos; não altere a geração de mundos que já possuem chunks.
- Teste exploração, teleporte, descarregamento, reinício e geração de chunks adicionais.
- O Multiverse gerencia a existência e os teletransportes; o Terra2 define a geração do mundo autorizado.

[Voltar ao índice](README.md)
