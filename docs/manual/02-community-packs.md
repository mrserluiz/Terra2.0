# Packs da comunidade do Terra original

Os **Community Packs Terra** utilizam o backend de compatibilidade. **Não** precisam passar por `/terra2 convert` nem virar arquivos `.terrapack`.

1. Baixe um pack compatível de sua fonte original. Exemplos de referência: [TerraOverworldConfig](https://github.com/PolyhedralDev/TerraOverworldConfig) e [Tartarus](https://github.com/PolyhedralDev/Tartarus).
2. Extraia os arquivos na pasta de Community Packs reconhecida pela instalação, normalmente `plugins/Terra2/packs/<pasta-do-pack>/`.
3. Verifique que `pack.yml` está diretamente na raiz do pack, sem uma pasta ZIP extra entre eles.
4. Confira no log se o Terra2 reconheceu o pack e qual **ID real** foi registrado; nome da pasta e ID não são necessariamente iguais.
5. Autorize um mundo novo em `worlds:` com `pack: ID_DO_PACK`, recarregue as configurações e crie o mundo com Multiverse.

Exemplo de layout:

```text
plugins/Terra2/packs/
  OVERWORLD/pack.yml
  TARTARUS/pack.yml
  MEU_PACK/pack.yml
```

**Compatibilidade não é garantida para todos os packs.** Dependências de addons, pipelines, biomas, samplers e TerraScript podem exigir ajustes. Um pack não deve ser considerado funcional apenas porque o carregamento não falhou: valide biomas, cavernas, estruturas, novos chunks e reinício.

**Atenção:** `plugins/Terra2/packs/` é a área dos packs Terra originais; `plugins/Terra2/terrapacks/` guarda conversões locais produzidas pelo Terra2. Não renomeie simplesmente ZIPs para `.terrapack`.

[Voltar ao índice](README.md)
