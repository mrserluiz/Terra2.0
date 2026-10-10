# Crear mundos con Multiverse-Core

[Manual del usuario](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core crea los mundos y Terra2 genera el terreno. Autoriza el mundo antes de recargar la configuración y crearlo. No cambies el generador de mundos existentes.


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
