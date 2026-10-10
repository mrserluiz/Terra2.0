# Multiverse-Core でワールドを作成

[ユーザーマニュアル](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core がワールドを作成し、Terra2 が地形を生成します。先にワールドを許可し、設定を再読み込みしてから作成します。既存ワールドの生成器は変更しないでください。


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
