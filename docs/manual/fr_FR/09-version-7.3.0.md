# Mise à jour Terra2 7.3.0

[Français — Manual](README.md) · **Terra2 7.3.0-BETA**

This page records commands and metadata verified in the supplied 7.3.0-BETA JAR. Earlier chapters retain historical information and may need further technical revision.

Plugin API: **26.2**. Declared Folia support. Optional dependencies: **Geyser-Spigot**, **WorldEdit**, **FastAsyncWorldEdit**.

## Commands available in the JAR

```text
/terra2 help
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 unlock <world> <pack[;pack]>
/terra2 reload
/terra2 packs list
/terra2 packs inspect <ID>
/terra2 convert <ID> <sources> [dimension]
/terra2 datapack list
/terra2 datapack inspect <file> [dimension]
/terra2 loot status
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
/terra2reportlog start|stop|status
```

## Important limits

- Authorizing a world does not create or regenerate it. Test new world bindings in disposable worlds.
- Biome and structure searches have bounded radius, time, or index coverage; no match does not establish absence.
- `/terra2 loot status` exists, but its presence alone does not confirm full implementation of planned per-world loot and item provenance.
- Java/Bedrock resource-pack visuals require separate validation; the Geyser soft dependency alone does not guarantee identical biome colors.
- Back up worlds before replacing the plugin JAR. Do not hot-swap builds.

**Source of verification:** `plugin.yml` and `lang/en_US.json` inside `Terra2-bukkit-7.3.0-BETA.jar`.
