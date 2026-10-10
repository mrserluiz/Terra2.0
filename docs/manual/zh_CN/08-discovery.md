# 识别和定位生物群系与结构 — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

半径：32..8192 方块。

索引按世界 UUID 和 seed 保存在 `plugins/Terra2/structure-index/`，每五秒及正常关服时保存。旧 pack 显示 `structures` 阶段的 feature ID；不会追溯重建模板或旧区块。原生结构显示原始注册 ID。每个世界最多保留 100,000 个位置。生物群系搜索在你的 Y 高度使用 32 方块网格，最多 16,384 次查询或查询间两秒，不生成区块。

已索引的结构锚点：`[ID, XYZ]`。已达到索引上限：`true/false`。仅包含已生成并索引的位置；不证明你在结构内。

找到采样点：`ID` [X, Y, Z]。在你的 Y 高度；32 方块网格，不保证最近。

在半径/时间/查询限制前未找到采样点（N 个采样点）。这不证明不存在。
