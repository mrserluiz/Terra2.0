# Commandes des mondes et traductions

Ce chapitre concerne la version 7.0.22-BETA ; les précédents conservent leur version de référence indiquée.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Community Packs enregistrés : OVERWORLD, TARTARUS, HYDRAXIA

Monde terra2_teste autorisé avec OVERWORLD. Configuration enregistrée ; génération activée pour les mondes autorisés.

Aucun monde créé ou régénéré. Pour un nouveau monde : /mv create terra2_teste normal -g Terra2:PACKS

Le monde world est protégé. Cette commande ne supprime aucune protection. Vérifiez le YAML manuellement ; les mondes vanilla principaux restent bloqués par le moteur.

Le monde terra2_teste est chargé. Son générateur ne peut pas être remplacé en cours de fonctionnement ; déchargez-le d’abord en toute sécurité.

Modifier le YAML ne contourne pas la protection du moteur pour les mondes vanilla principaux. Seul le premier pack peut fournir une base de terrain Community. Les autorisations enregistrées ne réécrivent pas les chunks existants et ne remplacent pas un plan de génération déjà associé.

## Autocomplétion, permissions et sécurité

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Choisissez la langue du serveur dans `plugins/Terra2/terra2-settings.yml`. Les commandes et les identifiants des packs restent inchangés. Les diagnostics détaillés existants conservent leur langue originale.

```yaml
language: fr_FR
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Community Packs détectés ; redémarrez le serveur pour les enregistrer : HYDRAXIA

[← README](README.md)
