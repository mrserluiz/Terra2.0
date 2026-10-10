# Lệnh thế giới và bản dịch

Chương này áp dụng cho 7.0.22-BETA; các chương trước giữ phiên bản tham chiếu đã ghi.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Community Pack đã đăng ký: OVERWORLD, TARTARUS, HYDRAXIA

Thế giới terra2_teste được phép dùng OVERWORLD. Đã lưu cấu hình; bật tạo địa hình cho các thế giới được phép.

Không tạo mới hoặc tạo lại thế giới nào. Với thế giới mới: /mv create terra2_teste normal -g Terra2:PACKS

Thế giới world được bảo vệ. Lệnh này không gỡ bảo vệ. Hãy kiểm tra YAML thủ công; các thế giới vanilla chính vẫn bị bộ máy chặn.

Thế giới terra2_teste đang được tải. Không thể thay bộ tạo địa hình khi đang hoạt động; hãy dỡ tải thế giới an toàn trước.

Chỉnh sửa YAML không vượt qua bảo vệ của bộ máy dành cho thế giới vanilla chính. Chỉ gói đầu tiên được làm nền địa hình Community. Quyền đã lưu không ghi lại chunk hiện có hoặc thay thế kế hoạch tạo địa hình đã gắn.

## Tự động hoàn thành, quyền và an toàn

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Chọn ngôn ngữ máy chủ trong `plugins/Terra2/terra2-settings.yml`. Lệnh và ID gói không thay đổi. Chẩn đoán chi tiết hiện có giữ nguyên ngôn ngữ gốc.

```yaml
language: vi_VN
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Đã phát hiện Community Pack; khởi động lại máy chủ để đăng ký: HYDRAXIA

[← README](README.md)
