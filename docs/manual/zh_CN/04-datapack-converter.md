# 原版数据包转换器

[用户手册](README.md) · **Terra2 7.0.14-BETA**

将包含 pack.mcmeta 的数据包 ZIP 或文件夹放入 plugins/Terra2/conversion/input/。运行转换并查看报告。READY 表示在支持范围内可运行；BLOCKED 不可运行。7.0.14 不是通用转换器。


```text
plugins/Terra2/conversion/input/
plugins/Terra2/conversion/reports/
plugins/Terra2/terrapacks/

/terra2 convert Plano flat-demo terra2_demo:flat
/terra2 convert Debris scatter-demo
/terra2 packs inspect Plano
/terra2 packs inspect Debris
```

[TerraPacks reference](../../../../terra2-engine/TERRAPACKS.md)
