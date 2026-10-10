# Tạo thế giới bằng Multiverse-Core

[Hướng dẫn sử dụng](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core tạo thế giới; Terra2 tạo địa hình. Cấp quyền cho thế giới trước khi tải lại cấu hình và tạo thế giới. Không đổi trình tạo của thế giới đã tồn tại.


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
