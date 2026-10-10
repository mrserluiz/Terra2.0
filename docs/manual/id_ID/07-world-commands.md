# Perintah dunia dan terjemahan

Bab ini berlaku untuk 7.0.22-BETA; bab sebelumnya tetap menggunakan versi acuan yang disebutkan.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Memvalidasi paket untuk dunia terra2_teste di latar belakang. Tidak ada dunia yang dibuat atau diubah.

Community Pack terdaftar: OVERWORLD, TARTARUS, HYDRAXIA

Dunia terra2_teste diizinkan menggunakan OVERWORLD. Pengaturan disimpan; generasi diaktifkan untuk dunia yang diizinkan.

Tidak ada dunia yang dibuat atau dibuat ulang. Untuk dunia baru: /mv create terra2_teste normal -g Terra2:PACKS

Dunia world dilindungi. Perintah ini tidak menghapus perlindungan. Periksa YAML secara manual; dunia vanilla utama tetap diblokir oleh mesin.

Dunia terra2_teste sedang dimuat. Generatornya tidak dapat diganti saat aktif; keluarkan dunia dari memori dengan aman terlebih dahulu.

Mengedit YAML tidak melewati perlindungan mesin untuk dunia vanilla utama. Hanya paket pertama yang boleh menjadi dasar medan Community. Izin yang disimpan tidak menulis ulang chunk yang ada atau mengganti rencana generasi yang sudah terikat.

## Pelengkapan otomatis, izin dan keamanan

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Pilih bahasa server di `plugins/Terra2/terra2-settings.yml`. Perintah dan ID paket tidak berubah. Diagnostik terperinci sebelumnya mempertahankan bahasa aslinya.

```yaml
language: id_ID
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Community Pack ditemukan; mulai ulang server untuk mendaftarkan: HYDRAXIA

[← README](README.md)
