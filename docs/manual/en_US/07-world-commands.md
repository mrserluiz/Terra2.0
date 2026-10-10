# World commands and translations

This chapter applies to 7.0.22-BETA; earlier chapters keep their stated reference build.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Validating packs for world terra2_teste in the background. No world is created or changed.

Registered Community Packs: OVERWORLD, TARTARUS, HYDRAXIA

World terra2_teste authorized with OVERWORLD. Settings saved; generation enabled for authorized worlds.

No world was created or regenerated. For a new world: /mv create terra2_teste normal -g Terra2:PACKS

World world is protected. This command cannot remove protection. Review YAML manually; primary vanilla worlds remain blocked by the engine.

World terra2_teste is loaded. Its generator cannot be replaced live; unload it safely first.

Editing YAML does not bypass the engine protection of primary vanilla worlds. Only the first pack may be a Community terrain base. Saved authorizations do not rewrite existing chunks or replace an already bound generation plan.

## Tab completion, permissions and safety

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Select the server language in `plugins/Terra2/terra2-settings.yml`. Command literals and pack IDs do not change. Existing detailed diagnostics retain their original language.

```yaml
language: en_US
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Community Packs discovered; restart the server to register: HYDRAXIA

[← README](README.md)
