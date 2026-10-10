# Lệnh và khắc phục sự cố

[Hướng dẫn sử dụng](README.md) · **Terra2 7.0.14-BETA**

Kiểm tra nhật ký, báo cáo Terra2 và báo cáo chuyển đổi. Thử chunk mới và khả năng lưu sau khi khởi động lại. Không thay JAR khi máy chủ đang chạy.


```text
/terra2 reload
/terra2 convert <ID> <input.zip> [namespace:dimension]
/terra2 convert <ID> <input-a.zip;input-b.zip> [namespace:dimension]
/terra2 packs list
/terra2 packs inspect <ID>
/terra2reportlog start
/mv create terra2_teste normal -g Terra2
/mv create terra2_mix_teste normal --generator Terra2:PACKS
/mv tp terra2_teste
```

```text
plugins/Terra2/reports/
plugins/Terra2/conversion/reports/<ID>.json
```
