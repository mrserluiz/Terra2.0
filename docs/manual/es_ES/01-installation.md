# Instalación y configuración

[Manual del usuario](README.md) · **Terra2 7.0.14-BETA**

Instala el JAR de Terra2 en plugins/ con el servidor apagado. Reinicia, activa la generación y autoriza un mundo nuevo de prueba en la configuración de mundos de Terra2. No sobrescribas el config.yml heredado.


```yaml
generation:
  enabled: true
  protected-worlds: [world, world_nether, world_the_end]
worlds:
  terra2_teste:
    pack: OVERWORLD
```
