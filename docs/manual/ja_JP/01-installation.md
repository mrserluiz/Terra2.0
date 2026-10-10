# インストールと設定

[ユーザーマニュアル](README.md) · **Terra2 7.0.14-BETA**

サーバー停止中に Terra2 JAR を plugins/ に配置します。再起動後、Terra2 のワールド設定で生成を有効化し、新しいテストワールドを許可してください。旧 Terra エンジンの config.yml を上書きしないでください。


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
