# Multiverse-Core ile dünya oluşturma

[Kullanıcı kılavuzu](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core dünyaları oluşturur; Terra2 araziyi üretir. Ayarları yeniden yükleyip dünyayı oluşturmadan önce izin verin. Mevcut dünyaların üreticisini değiştirmeyin.


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
