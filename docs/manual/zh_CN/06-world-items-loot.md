# 未来功能：按世界管理物品和战利品

[用户手册](README.md) · **Terra2 7.0.14-BETA**

规划中，7.0.14 尚未启用：按世界分配战利品、生成箱子的可信身份、物品来源、天然与玩家放置方块的区分，以及获取和使用限制。


```yaml
# FUTURE EXAMPLE ONLY — NOT SUPPORTED IN 7.0.14
world-loot:
  aeternum_aether:
    minecraft:chests/simple_dungeon:
      mode: REPLACE
      with: AETHER:DUNGEON
items:
  AETHER:DUNGEON_KEY:
    obtain-only-in: [aeternum_aether]
    usable-in: [aeternum_aether]
```
