# バイオームと構造物の識別・検索 — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

半径：32..8192 ブロック。

インデックスはワールド UUID と seed ごとに `plugins/Terra2/structure-index/` に保存され、5 秒ごとと正常終了時に更新されます。旧 pack は `structures` 段階の feature ID を表示し、テンプレートや古いチャンクは遡って再構築しません。ネイティブ構造物は元の登録 ID を表示します。各ワールド最大 100,000 件。バイオーム検索は現在の Y 高度、32 ブロック間隔、最大 16,384 回またはクエリ間 2 秒で、チャンクを生成しません。

記録された構造物の基準点：`[ID, XYZ]`。上限到達：`true/false`。生成済みで記録された位置のみ。内部にいる証明ではありません。

サンプルを発見：`ID` [X, Y, Z]。現在の Y 高度、32 ブロック間隔で、最寄りとは限りません。

半径・時間・検索数の制限までにサンプルなし（N サンプル）。存在しない証明ではありません。
