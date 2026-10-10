# Comandos e diagnóstico

| Comando | Função |
| --- | --- |
| `/terra2 reload` | Recarrega autorizações/configurações suportadas |
| `/terra2 convert <ID> <fonte> [namespace:dimension]` | Solicita conversão assíncrona |
| `/terra2 packs list` | Lista TerraPacks locais |
| `/terra2 packs inspect <ID>` | Exibe status, recursos e bloqueios da conversão |
| `/terra2reportlog start` | Inicia captura diagnóstica |
| `/mv create <mundo> normal -g Terra2` | Cria mundo via Multiverse usando vínculo autorizado |
| `/mv create <mundo> normal --generator Terra2:PACKS` | Cria mundo com composição configurada |
| `/mv tp <mundo>` | Teleporta via Multiverse |

**Os comandos `/mv` pertencem ao Multiverse-Core**, não ao Terra2. Não existe garantia de suporte a todos os aliases em todas as versões.

## Solução de problemas

1. **Pack não aparece:** confira `pack.yml`, pasta correta, ID e logs de addons/dependências.
2. **Mundo não autorizado:** verifique `generation.enabled`, `worlds` e lista de mundos protegidos.
3. **TerraPack BLOCKED:** consulte `/terra2 packs inspect <ID>` e `plugins/Terra2/conversion/reports/<ID>.json`.
4. **Falha ao gerar chunk:** preserve o log e consulte `plugins/Terra2/reports/` e [DIAGNOSTICS.md](../../terra2-engine/DIAGNOSTICS.md).
5. **Mudou pack/seed e não inicia:** manifests de geração protegem a identidade do mundo. Crie um mundo novo em vez de forçar troca.
6. **Mundo já explorado não muda:** somente novos chunks são gerados. Não espere regeneração automática.
7. **Atualização de JAR:** pare o servidor antes de substituir e reinicie por completo.

**Critério mínimo de aprovação:** carregamento, mundo novo, chunks variados, biomas/estruturas esperados, salvamento, reinício e nova exploração sem exceções.

[Voltar ao índice](README.md)
