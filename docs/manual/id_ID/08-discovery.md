# Mengidentifikasi dan menemukan bioma dan struktur — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Radius: 32..8192 blok.

Indeks disimpan berdasarkan UUID dan seed dunia di `plugins/Terra2/structure-index/`, setiap lima detik dan saat penutupan normal. Pack lama menampilkan ID feature pada tahap `structures`; template dan chunk lama tidak direkonstruksi. Struktur native menampilkan ID asli. Maksimum 100.000 posisi per dunia. Pencarian bioma pada Y Anda, kisi 32 blok, hingga 16.384 kueri atau dua detik di antara kueri. Tidak menghasilkan chunk.

Titik acuan terindeks: `[ID, XYZ]`. Batas indeks tercapai: `true/false`. Hanya posisi yang dihasilkan/diindeks; tidak membuktikan berada di dalam.

Sampel ditemukan: `ID` [X, Y, Z]. Pada Y Anda; kisi 32 blok, tidak menjamin yang terdekat.

Tidak ada sampel sebelum batas radius/waktu/kueri (N sampel). Ini tidak membuktikan ketiadaan.
