# 命令与故障排查

[用户手册](README.md) · **Terra2 7.0.14-BETA**

查看插件日志、Terra2 报告及转换报告。测试新区块及重启后的持久性。服务器运行时不要替换 JAR。


```text
/terra2 reload
/terra2 convert <ID> <input.zip> [namespace:dimension]
/terra2 convert <ID> <input-a.zip;input-b.zip> [namespace:dimension]
/terra2 packs list
/terra2 packs inspect <ID>
/terra2reportlog start
/mv create terra2_teste normal -g Terra2
/mv create terra2_mix_teste normal --generator Terra2:PACKS
/mv tp terra2_teste
```

```text
plugins/Terra2/reports/
plugins/Terra2/conversion/reports/<ID>.json
```
