# Tương lai: vật phẩm và chiến lợi phẩm theo thế giới

[Hướng dẫn sử dụng](README.md) · **Terra2 7.0.14-BETA**

Dự kiến, chưa hoạt động trong 7.0.14: loot theo thế giới, định danh rương được tạo, nguồn gốc vật phẩm, phân biệt khối tự nhiên và khối do người chơi đặt, cùng giới hạn nhận và sử dụng.


```yaml
# FUTURE EXAMPLE ONLY — NOT SUPPORTED IN 7.0.14
world-loot:
  aeternum_aether:
    minecraft:chests/simple_dungeon:
      mode: REPLACE
      with: AETHER:DUNGEON
items:
  AETHER:DUNGEON_KEY:
    obtain-only-in: [aeternum_aether]
    usable-in: [aeternum_aether]
```
