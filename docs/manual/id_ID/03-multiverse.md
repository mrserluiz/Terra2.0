# Membuat dunia dengan Multiverse-Core

[Panduan pengguna](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core membuat dunia; Terra2 menghasilkan medannya. Izinkan dunia sebelum memuat ulang pengaturan dan membuatnya. Jangan mengubah generator dunia yang sudah ada.


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
