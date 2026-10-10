# Identificar y localizar biomas y estructuras — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Radio: 32..8192 bloques.

El índice se guarda por UUID y seed del mundo en `plugins/Terra2/structure-index/`, cada cinco segundos y al apagar normalmente. Los packs antiguos muestran IDs de features colocadas en la etapa `structures`; no se reconstruyen templates ni chunks anteriores. Las estructuras nativas muestran sus IDs originales. Límite: 100.000 posiciones por mundo. La búsqueda de biomas usa tu Y, cuadrícula de 32 bloques y hasta 16.384 consultas o dos segundos entre consultas. No genera chunks.

Anclas de estructuras indexadas: `[ID, XYZ]`. Límite del índice alcanzado: `true/false`. Solo posiciones generadas/indexadas; no demuestra estar dentro.

Muestra encontrada: `ID` [X, Y, Z]. A tu Y; cuadrícula de 32 bloques, sin garantizar el más cercano.

Sin muestra antes del límite de radio/tiempo/consultas (N muestras). No demuestra ausencia.
