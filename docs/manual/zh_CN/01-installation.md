# 安装与配置

[用户手册](README.md) · **Terra2 7.0.14-BETA**

关闭服务器后，将 Terra2 JAR 放入 plugins/。重启服务器，在 Terra2 世界配置中启用生成并授权新的测试世界。不要覆盖旧版 Terra 引擎的 config.yml。


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
