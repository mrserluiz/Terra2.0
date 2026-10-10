# Conversor de datapacks vanilla → TerraPacks

**Referência: 7.0.14-BETA.** O conversor é experimental e deliberadamente restrito. Não é um conversor universal de datapacks com estruturas, jigsaw, biomas personalizados ou noise/density avançado.

## Diretórios

```text
plugins/Terra2/conversion/input/          # fontes ZIP ou pastas
plugins/Terra2/conversion/reports/        # relatório por ID
plugins/Terra2/terrapacks/                # saída .terrapack
```

A fonte precisa conter `pack.mcmeta` na raiz ou em exatamente uma pasta envolvente. Não envie uma coleção de vários datapacks como se fosse um único datapack. Prefira nomes simples sem espaços.

## Converter e inspecionar

```text
/terra2 convert <ID> <input.zip> [namespace:dimension]
/terra2 convert <ID> <input-a.zip;input-b.zip> [namespace:dimension]
/terra2 packs list
/terra2 packs inspect <ID>
```

O processo é assíncrono e executa um trabalho de cada vez. Não sobrescreva IDs de conversão existentes; use um novo ID. O comando utiliza a permissão `terra2.settings.reload` (OP por padrão).

- **READY:** o conjunto de recursos semânticos suportados foi compilado; ainda exige validação de blocos/biomas e teste em servidor.
- **BLOCKED:** recursos necessários não são suportados. O arquivo e relatório podem existir, mas **não** são geradores executáveis.

Na 7.0.14, a geração convertida cobre um subconjunto restrito de `minecraft:flat` e extensões de features `minecraft:simple_block` com condições específicas. Jigsaw, NBT de estruturas executável, processors, loot, funções e receitas não são suporte completo dessa build.

## Teste integrado disponibilizado pela build

Após instalar a build de referência, ela pode criar os exemplos `flat-demo` e `scatter-demo` caso não existam. Execute separadamente, aguardando cada conversão:

```text
/terra2 convert Plano flat-demo terra2_demo:flat
/terra2 convert Debris scatter-demo
/terra2 packs inspect Plano
/terra2 packs inspect Debris
```

Autorize um mundo novo:

```yaml
worlds:
  terra2_mix_teste:
    packs: [Plano, Debris]
```

E execute:

```text
/terra2 reload
/terra2reportlog start
/mv create terra2_mix_teste normal --generator Terra2:PACKS
/mv tp terra2_mix_teste
```

Resultado esperado na receita de referência: superfície plana de grama em Y=0 e blocos dispersos de `mossy_cobblestone` nos novos chunks.

**Versões experimentais posteriores:** a documentação de desenvolvimento descreve checkpoints 7.0.15–7.0.18 com diagnóstico de NBT, migração de templates e interfaces de execução. Esses avanços **não significam** que o Dungeons and Taverns já esteja READY ou que a execução completa de estruturas/loot esteja conectada e validada.

Para detalhes e limitações exatas, consulte [TERRAPACKS.md](../../terra2-engine/TERRAPACKS.md).

[Voltar ao índice](README.md)
