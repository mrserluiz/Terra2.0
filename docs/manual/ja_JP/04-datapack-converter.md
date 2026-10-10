# バニラデータパック変換

[ユーザーマニュアル](README.md) · **Terra2 7.0.14-BETA**

pack.mcmeta を含むデータパック ZIP またはフォルダーを plugins/Terra2/conversion/input/ に配置します。変換後にレポートを確認してください。READY は対応範囲内で実行可能、BLOCKED は実行不可です。7.0.14 は汎用変換器ではありません。


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
