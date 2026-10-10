# 将来予定：ワールド別アイテムと戦利品

[ユーザーマニュアル](README.md) · **Terra2 7.0.14-BETA**

計画段階であり 7.0.14 では未実装：ワールド別戦利品、生成チェストの識別、アイテムの出所、自然ブロックと設置ブロックの区別、入手・使用制限。


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
