# 世界命令与翻译

本章适用于 7.0.22-BETA；之前的章节保留其标明的参考版本。

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

正在后台验证世界 terra2_teste 的包。不会创建或更改任何世界。

已注册的 Community Packs：OVERWORLD, TARTARUS, HYDRAXIA

已授权世界 terra2_teste 使用 OVERWORLD。设置已保存，并已为获准世界启用生成。

未创建或重新生成任何世界。新建世界请使用：/mv create terra2_teste normal -g Terra2:PACKS

世界 world 受到保护。此命令不能移除保护。请手动检查 YAML；引擎仍会阻止在主要原版世界中生成。

世界 terra2_teste 已加载。无法在运行时更换其生成器；请先安全卸载该世界。

编辑 YAML 不会绕过引擎对主要原版世界的保护。只有第一个包可以作为 Community 地形基础。保存授权不会重写现有区块，也不会替换已经绑定的生成计划。

## 自动补全、权限与安全

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

在 `plugins/Terra2/terra2-settings.yml` 中选择服务器语言。命令和包 ID 不变。已有的详细诊断保留原始语言。

```yaml
language: zh_CN
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

发现 Community Packs；请重启服务器以注册：HYDRAXIA

[← README](README.md)
