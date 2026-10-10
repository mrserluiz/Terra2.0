# Kurulum ve yapılandırma

[Kullanıcı kılavuzu](README.md) · **Terra2 7.0.14-BETA**

Sunucu kapalıyken Terra2 JAR dosyasını plugins/ klasörüne kurun. Yeniden başlatın, dünya oluşturmayı etkinleştirin ve Terra2 dünya ayarlarında yeni bir test dünyasına izin verin. Eski Terra motorunun config.yml dosyasının üzerine yazmayın.


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
