# Futuro: itens e loot por mundo/dimensão

> **Roadmap — não é funcionalidade ativa da build de referência 7.0.14.** A documentação técnica posterior descreve fundamentos experimentais de política/recibo de origem, mas não a integração completa de loot, inventário e eventos Paper. Os exemplos abaixo não são YAML suportado atualmente.

## Objetivo

Permitir que cada mundo ou dimensão tenha minérios, blocos, loot tables, baús e itens exclusivos sem transformar itens comuns trazidos de outros mundos.

## Componentes previstos

1. **World Loot Router:** selecionar `ALLOW`, `REPLACE` ou `BLOCK` por mundo, pack, estrutura e loot table.
2. **Identidade confiável de contêiner:** só baús registrados durante geração legítima de estruturas recebem loot dimensional. Baús colocados por jogadores não recebem identidade automática.
3. **Item Provenance:** atribuir ID e origem persistentes no instante confiável de criação (estrutura, bloco natural, entidade ou recompensa autorizada); jogar um item no chão não deve emitir nova origem.
4. **Natural Block Tracker:** diferenciar blocos naturais de blocos colocados ou alterados por jogadores, com armazenamento eficiente de exceções e testes para explosões, pistões, fluidos e editores de mundo.
5. **Políticas separadas:** `obtain-only-in`, `usable-in` e regras de transporte, sem destruir itens do jogador.
6. **Integração com o adaptador vanilla:** preservar referências de `structure_set`, `template_pool`, `processor_list`, `.nbt` e `loot_table` e executar somente quando o grafo completo estiver suportado.

## Cenários que precisam funcionar

| Situação | Resultado planejado |
| --- | --- |
| Minerar minério natural do Aether | Gerar minério com identidade Aether |
| Levar minério do Overworld, colocar e minerar no Aether | Manter identidade normal; não converter |
| Abrir baú de dungeon gerado no Aether | Aplicar loot table autorizada do Aether |
| Colocar baú comum no Aether | Não gerar loot especial |
| Transportar chave exclusiva do Aether | Preservar item; impedir uso fora do Aether quando configurado |
| Reiniciar servidor e reabrir baú | Não duplicar nem rerrolar loot |

## Exemplo **conceitual** de configuração futura

```yaml
# ILUSTRATIVO — NÃO É CONFIGURAÇÃO SUPORTADA
world-loot:
  aeternum_aether:
    minecraft:chests/simple_dungeon:
      mode: REPLACE
      with: AETHER:DUNGEON

items:
  AETHER:DUNGEON_KEY:
    obtain-only-in: [aeternum_aether]
    usable-in: [aeternum_aether]
    transferable: true
```

## Critérios antes de liberar

- Persistência em chunks, reinício, inventários, baús, funis, shulkers, morte e portais.
- Proteção contra remarcação por drop, colocação, quebra, transferência e manipulação de contêineres.
- Identidade de origem verificável; nenhum item comum vira especial apenas por entrar em outro mundo.
- Compatibilidade com loot vanilla e datapacks sem registrar recursos globais não autorizados.
- Testes reais com Paper e geração/reinício, além de testes automatizados.

[Voltar ao índice](README.md)
