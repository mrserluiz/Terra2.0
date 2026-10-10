# Dünya komutları ve çeviriler

Bu bölüm 7.0.22-BETA için geçerlidir; önceki bölümler belirtilen referans sürümünü korur.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Kayıtlı Community Pack paketleri: OVERWORLD, TARTARUS, HYDRAXIA

terra2_teste dünyası için OVERWORLD yetkilendirildi. Ayarlar kaydedildi; yetkili dünyalarda üretim etkinleştirildi.

Hiçbir dünya oluşturulmadı veya yeniden üretilmedi. Yeni bir dünya için: /mv create terra2_teste normal -g Terra2:PACKS

world dünyası korunuyor. Bu komut korumayı kaldırmaz. YAML dosyasını elle inceleyin; ana vanilla dünyaları motor tarafından engellenmeye devam eder.

terra2_teste dünyası yüklü. Çalışırken üreticisi değiştirilemez; önce dünyayı güvenli şekilde bellekten çıkarın.

YAML dosyasını düzenlemek ana vanilla dünyaları için motor korumasını aşmaz. Yalnızca ilk paket Community arazi tabanı olabilir. Kaydedilen yetkiler mevcut chunk verilerini yeniden yazmaz ve bağlı bir üretim planını değiştirmez.

## Otomatik tamamlama, izinler ve güvenlik

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Sunucu dilini `plugins/Terra2/terra2-settings.yml` dosyasında seçin. Komutlar ve paket kimlikleri değişmez. Mevcut ayrıntılı tanılama çıktıları özgün dilini korur.

```yaml
language: tr_TR
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Community Pack paketleri bulundu; kaydetmek için sunucuyu yeniden başlatın: HYDRAXIA

[← README](README.md)
