# Creating worlds with Multiverse-Core

[User manual](README.md) · **Terra2 7.0.14-BETA**

Multiverse-Core creates worlds; Terra2 supplies generation. Authorize the world first, then reload settings and create it. Do not rebind existing worlds.


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
