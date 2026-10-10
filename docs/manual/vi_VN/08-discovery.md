# Nhận diện và tìm biome và cấu trúc — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Bán kính: 32..8192 khối.

Chỉ mục lưu theo UUID và seed thế giới tại `plugins/Terra2/structure-index/`, mỗi năm giây và khi tắt bình thường. Pack cũ hiển thị ID feature của giai đoạn `structures`; template và chunk cũ không được tái dựng. Cấu trúc native hiển thị ID gốc. Tối đa 100.000 vị trí mỗi thế giới. Tìm biome tại Y của bạn, lưới 32 khối, tối đa 16.384 truy vấn hoặc hai giây giữa truy vấn. Không tạo chunk.

Điểm neo cấu trúc đã lập chỉ mục: `[ID, XYZ]`. Đã đạt giới hạn: `true/false`. Chỉ vị trí đã tạo/lập chỉ mục; không chứng minh bạn ở bên trong.

Tìm thấy mẫu: `ID` [X, Y, Z]. Tại Y của bạn; lưới 32 khối, không đảm bảo gần nhất.

Không có mẫu trước giới hạn bán kính/thời gian/truy vấn (N mẫu). Không chứng minh rằng không tồn tại.
