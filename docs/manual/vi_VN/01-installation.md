# Cài đặt và cấu hình

[Hướng dẫn sử dụng](README.md) · **Terra2 7.0.14-BETA**

Cài JAR Terra2 vào plugins/ khi máy chủ đã dừng. Khởi động lại, bật tạo địa hình và cấp quyền cho thế giới thử nghiệm mới trong cấu hình thế giới Terra2. Không ghi đè config.yml cũ của Terra.


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
