# Identificare e localizzare biomi e strutture — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Raggio: 32..8192 blocchi.

L’indice è salvato per UUID e seed del mondo in `plugins/Terra2/structure-index/`, ogni cinque secondi e allo spegnimento normale. I vecchi pack mostrano gli ID delle feature della fase `structures`; template e vecchi chunk non sono ricostruiti. Le strutture native mostrano gli ID originali. Massimo 100.000 posizioni per mondo. Ricerca biomi alla tua Y, griglia di 32 blocchi, fino a 16.384 query o due secondi tra query. Nessuna generazione di chunk.

Ancore indicizzate: `[ID, XYZ]`. Limite raggiunto: `true/false`. Solo posizioni generate/indicizzate; non dimostra di essere dentro.

Campione trovato: `ID` [X, Y, Z]. Alla tua Y; griglia di 32 blocchi, senza garanzia del più vicino.

Nessun campione prima del limite di raggio/tempo/query (N campioni). Non dimostra assenza.
