#!/usr/bin/env bash
set -eu
while IFS= read -r candidate; do
  if jar tf "$candidate" 2>/dev/null | rg -q '^net/minecraft/world/level/levelgen/structure/Structure.class$'; then
    javap -private -classpath "$candidate" \
      net.minecraft.world.level.chunk.ChunkGenerator \
      net.minecraft.world.level.chunk.ChunkGeneratorStructureState \
      net.minecraft.world.level.levelgen.structure.Structure \
      net.minecraft.world.level.levelgen.structure.StructureStart \
      net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager \
      net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate \
      net.minecraft.resources.RegistryDataLoader \
      net.minecraft.resources.RegistryOps \
      net.minecraft.core.RegistryAccess \
      net.minecraft.core.HolderLookup\$Provider \
      net.minecraft.core.MappedRegistry \
      net.minecraft.server.MinecraftServer \
      net.minecraft.server.ReloadableServerRegistries \
      net.minecraft.server.packs.resources.ResourceManager \
      net.minecraft.server.packs.resources.MultiPackResourceManager \
      net.minecraft.server.packs.AbstractPackResources \
      net.minecraft.server.packs.PackResources \
      net.minecraft.server.packs.resources.Resource \
      net.minecraft.world.level.storage.loot.LootTable \
      net.minecraft.world.level.storage.loot.LootParams \
      net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement
    exit 0
  fi
done < <(rg --files . "$HOME/.gradle/caches" -g '*.jar')
echo 'Paper worldgen classes not found' >&2
exit 1
