# Comandi dei mondi e traduzioni

Questo capitolo riguarda 7.0.22-BETA; i capitoli precedenti mantengono la build di riferimento indicata.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Community Pack registrati: OVERWORLD, TARTARUS, HYDRAXIA

Mondo terra2_teste autorizzato con OVERWORLD. Configurazione salvata; generazione attivata per i mondi autorizzati.

Nessun mondo è stato creato o rigenerato. Per un mondo nuovo: /mv create terra2_teste normal -g Terra2:PACKS

Il mondo world è protetto. Questo comando non rimuove le protezioni. Controlla il file YAML manualmente; i mondi vanilla principali restano bloccati dal motore.

Il mondo terra2_teste è caricato. Non è possibile sostituire il generatore mentre è attivo; scarica prima il mondo in sicurezza.

La modifica del YAML non aggira la protezione del motore per i mondi vanilla principali. Solo il primo pack può essere una base di terreno Community. Le autorizzazioni salvate non riscrivono chunk esistenti e non sostituiscono un piano di generazione già associato.

## Completamento automatico, permessi e sicurezza

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Scegli la lingua del server in `plugins/Terra2/terra2-settings.yml`. I comandi e gli ID dei pack non cambiano. La diagnostica dettagliata precedente mantiene la lingua originale.

```yaml
language: it_IT
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Community Pack rilevati; riavvia il server per registrarli: HYDRAXIA

[← README](README.md)
