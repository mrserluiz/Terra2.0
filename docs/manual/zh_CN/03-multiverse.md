# 使用 Multiverse-Core 创建世界

[用户手册](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core 负责创建世界，Terra2 负责地形生成。先授权世界，再重载配置并创建。不要更改现有世界的生成器。


```yaml
worlds:
  terra2_teste:
    pack: OVERWORLD
  terra2_mix_teste:
    packs: [OVERWORLD, Debris]
```

```text
/terra2 reload
/mv create terra2_teste normal -g Terra2
/mv tp terra2_teste
/mv create terra2_mix_teste normal --generator Terra2:PACKS
```
