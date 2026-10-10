# Commands and troubleshooting

[User manual](README.md) · **Terra2 7.0.14-BETA**

Check the plugin logs, Terra2 reports and conversion reports. Always test new chunks and restart persistence. Do not hot-swap the plugin JAR.


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
