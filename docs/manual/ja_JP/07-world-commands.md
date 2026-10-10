# ワールドコマンドと翻訳

この章は 7.0.22-BETA に対応しています。以前の章は記載された参照バージョンを維持します。

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

登録済み Community Packs：OVERWORLD, TARTARUS, HYDRAXIA

ワールド terra2_teste に OVERWORLD の使用を許可しました。設定を保存し、許可済みワールドの生成を有効にしました。

ワールドの作成や再生成は行っていません。新しいワールドには：/mv create terra2_teste normal -g Terra2:PACKS

ワールド world は保護されています。このコマンドでは保護を解除できません。YAML を手動で確認してください。主要なバニラワールドは引き続きエンジンがブロックします。

ワールド terra2_teste は読み込み中です。稼働中に生成器を変更できません。先に安全にワールドをアンロードしてください。

YAML の編集では、主要なバニラワールドに対するエンジンの保護を回避できません。Community 地形ベースにできるのは最初のパックだけです。保存した許可設定は既存のチャンクを書き換えず、関連付け済みの生成計画も置き換えません。

## 自動補完、権限、安全性

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

`plugins/Terra2/terra2-settings.yml` でサーバーの言語を選択します。コマンドとパック ID は変わりません。既存の詳細な診断は元の言語を維持します。

```yaml
language: ja_JP
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Community Packs を検出しました。登録するにはサーバーを再起動してください：HYDRAXIA

[← README](README.md)
