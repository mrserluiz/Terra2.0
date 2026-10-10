# Biyom ve yapıları tanımlama ve bulma — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Yarıçap: 32..8192 blok.

Dizin, dünya UUID ve seed bilgileriyle `plugins/Terra2/structure-index/` altında her beş saniyede ve normal kapanışta kaydedilir. Eski packler `structures` aşamasındaki feature kimliklerini gösterir; şablonlar ve eski chunklar geriye dönük oluşturulmaz. Native yapılar özgün kimliklerini gösterir. Dünya başına en fazla 100.000 konum. Biyom araması Y seviyenizde, 32 blok ızgarada, en fazla 16.384 sorgu veya sorgular arasında iki saniye. Chunk üretilmez.

Dizindeki yapı dayanakları: `[ID, XYZ]`. Dizin sınırına ulaşıldı: `true/false`. Yalnızca üretilmiş/dizine alınmış konumlar; içeride olduğunuzu kanıtlamaz.

Örnek bulundu: `ID` [X, Y, Z]. Y seviyenizde; 32 blok ızgara, en yakın olduğu garanti edilmez.

Yarıçap/süre/sorgu sınırından önce örnek bulunamadı (N örnek). Yokluğu kanıtlamaz.
