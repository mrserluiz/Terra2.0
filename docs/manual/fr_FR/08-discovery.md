# Identifier et localiser les biomes et structures — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Rayon : 32..8192 blocs.

L’index est sauvegardé par UUID et seed du monde dans `plugins/Terra2/structure-index/`, toutes les cinq secondes et à l’arrêt normal. Les anciens packs exposent les IDs des features placées à l’étape `structures` ; les templates et anciens chunks ne sont pas reconstruits. Les structures natives exposent leurs IDs d’origine. Limite : 100 000 positions par monde. Recherche de biomes à votre Y, grille de 32 blocs, maximum 16 384 requêtes ou deux secondes entre requêtes. Aucun chunk généré.

Ancres indexées : `[ID, XYZ]`. Limite atteinte : `true/false`. Positions générées/indexées seulement ; ne prouve pas que vous êtes à l’intérieur.

Échantillon trouvé : `ID` [X, Y, Z]. À votre Y ; grille de 32 blocs, sans garantie du plus proche.

Aucun échantillon avant la limite de rayon/temps/requêtes (N échantillons). Cela ne prouve pas une absence.
