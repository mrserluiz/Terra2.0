# Terra 2.0

**Languages:** English | [Português (Brasil)](docs/manual/README.md)

Terra 2.0 is an independent continuation and modernization of the Terra world-generation engine for Paper, focused on **per-world generation**, **Community Pack compatibility**, safe world isolation, and a path toward native Minecraft datapack support.

> **Current reference build:** `Terra2-bukkit-7.3.1-BETA.jar` — Paper 26.2, official local Geyser integration, persistent biome edits and per-world Bedrock activation.

[7.3.1 release notes](docs/releases/7.3.1-BETA.md) (native Bedrock bridge verified with official Geyser 2.11.3 build 1251; 2.12 not supported) · [Changes since 7.0.25](docs/releases/7.0.30-BETA.md) · [Bedrock setup and limits](docs/BEDROCK_BIOMES.md)

Terra 2.0 is no longer only a recovery scaffold. The 7.0.25 line has an executable generation core, a Terra Community Pack compatibility backend, a first vanilla datapack importer, local TerraPacks, and per-world pack composition.

## What works in 7.0.25

- Terra Community Packs can be selected independently per authorized world.
- Existing Terra generation remains available through the compatibility backend.
- Generation is routed through the standalone Terra2 core and immutable world bindings.
- World authorization is explicit and generation is disabled by default.
- The primary vanilla worlds remain protected.
- A first vanilla datapack importer compiles the supported `minecraft:flat` subset.
- Local `.terrapack` archives distinguish `READY`, `READY_NATIVE`, and `BLOCKED` conversions.
- A world can compose one terrain base with supported additive feature packs.
- Conversion is asynchronous and preserves source/provenance data.
- Generation manifests/fingerprints protect worlds from silent pack/source changes.
- Supported converted structure packs use native jigsaw assembly, processors and loot.
- New Community Pack worlds can keep Terra2 identities in chunk PDC while exposing vanilla-equivalent biomes to Bukkit.
- Diagnostic capture is available through `/terra2reportlog`.

## Vanilla biome compatibility (7.0.25-BETA)

New Community Pack worlds can opt into `biomes.mode: VANILLA_COMPATIBLE`. Minecraft/Bukkit receives the pack's vanilla biome reference, while Terra2 keeps original three-dimensional identities in each chunk's persistent data. The built-in API and `/terra2 biome` read these saved identities; other plugins can keep using ordinary Bukkit biome queries. Existing saves are not converted. See [configuration, persistence and limitations](docs/BIOME_COMPATIBILITY.md).

## Optional climate metadata API (7.0.24-BETA)

Terra2 publishes read-only climate profiles per authorized world. The separate **Terra2AeternumBridge** addon connects only when a public compatible receiver contract is detected; neither plugin requires the other. Keep the author's official AeternumSeasons distribution. Official climate-profile receiver compatibility is not established; an unsupported receiver remains untouched and inactive. Profiles live in Terra2 settings, keep real biome IDs, and can be reloaded without unloading plugins. See [configuration, API and downloads](docs/CLIMATE_API.md) and the [Paper integration verification](docs/testing/CLIMATE_INTEGRATION_7.0.24.md).

## Community Packs

Known Terra-style packs remain the primary compatibility path. A world may select a single legacy pack directly:

```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]

worlds:
  terra2_teste:
    pack: OVERWORLD
```

Create it with Multiverse using the configured pack ID:

```text
/mv create terra2_teste normal -g Terra2
```

Exact generator syntax may depend on the world-management plugin and the selected Terra2 configuration path. Existing worlds should not be rebound to a different generation plan.

## TerraPacks and composition

7.0.14 introduces an immutable local TerraPack format and composition model. One terrain base can be combined with supported additive features:

```yaml
worlds:
  terra2_mix_teste:
    packs: [OVERWORLD, Debris]
```

For explicit composition, use:

```text
/mv create terra2_mix_teste normal --generator Terra2:PACKS
```

Conversion commands:

```text
/terra2 convert <ID> <input.zip> [namespace:dimension]
/terra2 convert <ID> <input-a.zip;input-b.zip> [namespace:dimension]
/terra2 packs list
/terra2 packs inspect <ID>
/terra2 reload
```

The 7.0.14 converter is intentionally strict. Unsupported semantic resources produce a `BLOCKED` TerraPack instead of being silently ignored.

See [TERRAPACKS.md](terra2-engine/TERRAPACKS.md) for the exact supported subset and test procedure.

## Safety model

Terra2 does not attach generation globally. A target world must be explicitly authorized and bound to a generation plan. The server primary world and vanilla dimensions are protected by default. Active bindings reject changes to source, pack selection, dimension, seed, or height bounds.

Terrain generation applies to new chunks. The explicit `/terra2 setbiome` command can edit biome cells in existing loaded chunks without regenerating their blocks.

## Project layout

- `terra2-engine/` — recovered Terra engine plus the standalone Terra2 generation core and Paper integration.
- `terra2-engine/common/implementation/terra2-core/` — platform-independent generation routing and composition contracts.
- `terra2-engine/TERRAPACKS.md` — TerraPack conversion/composition reference.
- `terra2-engine/CORE-ARCHITECTURE.md` — executable core architecture and version milestones.
- `terra2-engine/DIAGNOSTICS.md` — crash/stall/console capture diagnostics.
- `ENGINE-MIGRATION.md` — migration history, safety rules and roadmap.
- `BUILD-STATUS.md` — current reference-build status and limitations.

## Current limitations

For the current build, consult [7.0.30 release notes](docs/releases/7.0.30-BETA.md). Native Bedrock visuals currently require CUSTOM worlds and official local Geyser-Spigot. Actual WorldEdit selection and Bedrock reconnect acceptance remain server tests; neither is claimed as CI client validation. The following paragraphs describe the older 7.0.14 converter milestone.

7.0.14 is **not a universal vanilla datapack converter**. Complex noise/density/surface graphs, custom biome/dimension types, jigsaw/template execution, processors, world-scoped loot and other advanced resources are not complete in this reference build.

Terra Community Packs and vanilla datapack conversion are separate paths: Community Packs use the compatibility backend; supported vanilla resources compile into Terra2's neutral generation model.

Development notes for post-7.0.14 structure/NBT/loot work may already exist in the repository, but they must not be interpreted as capabilities of the 7.0.14 reference JAR until integrated and server-validated.

## Development direction

The long-term goal is to make Terra2 a world-generation platform where each world can independently define terrain, biomes, structures, features and eventually world-scoped loot/item behavior, while retaining compatibility with the Terra pack ecosystem.

Terra2 is a successor project and is not an official release of the original Terra project.

## 7.0.22-BETA: world authorization commands

`/terra2 lis Cpack` lists registered Community Packs and rejected/pending sources. `/terra2 unlock <world> <pack>` validates and saves authorization without creating a world. Base worlds, configured protections and live generator replacement remain blocked. Tab completion covers command literals, worlds, pack IDs and input files. New command help and authorization responses support 13 languages through `language` in `terra2-settings.yml`; existing detailed diagnostics retain their original language and causes.

## User manual languages

- [Português (Brasil) (pt_BR)](docs/manual/README.md)
- [English (en_US)](docs/manual/en_US/README.md)
- [Español (es_ES)](docs/manual/es_ES/README.md)
- [Bahasa Indonesia (id_ID)](docs/manual/id_ID/README.md)
- [Italiano (it_IT)](docs/manual/it_IT/README.md)
- [Français (fr_FR)](docs/manual/fr_FR/README.md)
- [Deutsch (de_DE)](docs/manual/de_DE/README.md)
- [Русский (ru_RU)](docs/manual/ru_RU/README.md)
- [Polski (pl_PL)](docs/manual/pl_PL/README.md)
- [Tiếng Việt (vi_VN)](docs/manual/vi_VN/README.md)
- [Türkçe (tr_TR)](docs/manual/tr_TR/README.md)
- [简体中文 (zh_CN)](docs/manual/zh_CN/README.md)
- [日本語 (ja_JP)](docs/manual/ja_JP/README.md)

## Paleta visual genérica Bedrock (7.0.27-BETA, preparatória)

`/terra2 bedrock pack` gera um atlas RGB com uma base de 256 cores e as cores exatas solicitadas pelos Community Packs e providers externos (`VisualPaletteProvider`). Deduplica por RGB e preserva packs/mappings de terceiros. Mantém o Geyser oficial; instalação/compilação do fork experimental foi retirada. A seleção visual por posição está **BLOCKED_PENDING_POSITION_TRANSPORT**: o pack não ativa sozinho as cores dos biomas. [Formato, API, comandos e limites](docs/BEDROCK_BIOMES.md).

## 7.0.28-BETA: Bedrock grass position preview

Keep official local Geyser-Spigot. `/terra2 bedrock prepare`, restart, accept the automatically offered RGB pack, then `/terra2 bedrock preview` as a Bedrock OP in an authorized Terra2 world. `/terra2 bedrock stop` restores normal visuals; `/terra2 bedrock disable` removes startup authorization. Bounded solid-color grass preview only: 64 nearby blocks, two participants, no saved-world changes or global Java mappings. Uses saved Terra2 identities in projected worlds and explicitly labeled provider prediction in CUSTOM worlds. Position transport is a guarded adapter to Geyser internals, not a stable public API; incompatible versions fail closed. [Instructions and limits](docs/BEDROCK_BIOMES.md).

### Teste Bedrock 7.0.29-BETA

Ponte embutida para Geyser-Spigot oficial no Paper 26.2. O novo `/terra2 bedrock native` testa cores de água, grama e folhagem via client biomes, mantendo texturas e líquidos vanilla. Exige OP, mundo autorizado CUSTOM e pack aceito. Transporte/pacotes e reinício verificados na CI; renderização/aceitação tardia no cliente ainda experimental. Esse procedimento descreve a 7.0.29; na 7.0.30 há reenvio de chunks próximos e teleporte dentro do mesmo mundo mantém a ativação. Não atende VANILLA_COMPATIBLE nem recupera IDs Terra que outro plugin tenha substituído por biomas vanilla. [Procedimento e limites](docs/BEDROCK_BIOMES.md#biomas-visuais-nativos--7029-beta-experimental).

### Build 7.0.30-BETA

`/terra2 setbiome <ID>` aplica o bioma à seleção cuboide do WorldEdit/FAWE em mundos Terra2 autorizados, com identidade persistente no chunk, limite de volume e proteção do mundo principal. Não regenera terreno e não usa o undo do WorldEdit.

`/terra2 bedrock native` passa a lembrar a ativação por UUID/seed/fingerprint do mundo e reaplicá-la aos jogadores Bedrock ao reconectar. Reenvio limitado de chunks carregados, manifesto versionado pela build e banner de grama no console. `/terra2 bedrock forget` remove a ativação persistente do mundo. Ponte nativa ainda exige CUSTOM. [Teste e limitações](docs/BEDROCK_BIOMES.md). [CI aprovada](https://github.com/mrserluiz/Terra2.0/actions/runs/38341188293); seleção WorldEdit real e reconexão do cliente Bedrock precisam de validação no servidor.
