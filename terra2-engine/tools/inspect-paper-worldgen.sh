#!/usr/bin/env bash
set -eu
while IFS= read -r candidate; do
  if jar tf "$candidate" 2>/dev/null | grep '^net/minecraft/world/level/levelgen/structure/Structure.class$' >/dev/null; then
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
      net.minecraft.core.HolderLookup\$RegistryLookup \
      net.minecraft.core.HolderGetter\$Provider \
      net.minecraft.core.Holder\$Reference \
      net.minecraft.core.MappedRegistry \
      net.minecraft.core.component.DataComponentInitializers \
      net.minecraft.core.component.DataComponentInitializers\$PendingComponents \
      net.minecraft.nbt.CompoundTag \
      net.minecraft.nbt.NbtIo \
      net.minecraft.world.level.levelgen.structure.Structure\$GenerationContext \
      net.minecraft.world.level.levelgen.structure.Structure\$GenerationStub \
      net.minecraft.server.MinecraftServer \
      net.minecraft.server.ReloadableServerRegistries \
      net.minecraft.server.packs.resources.ResourceManager \
      net.minecraft.server.packs.resources.MultiPackResourceManager \
      net.minecraft.server.packs.AbstractPackResources \
      net.minecraft.server.packs.PackResources \
      net.minecraft.server.packs.resources.Resource \
      net.minecraft.world.level.storage.loot.LootTable \
      net.minecraft.world.level.storage.loot.LootParams \
      net.minecraft.world.level.storage.loot.LootContext \
      net.minecraft.world.level.storage.loot.LootContext\$Builder \
      net.minecraft.server.ReloadableServerRegistries\$Holder \
      net.minecraft.resources.RegistryDataLoader\$RegistryData \
      net.minecraft.resources.RegistryOps\$RegistryInfo \
      net.minecraft.resources.RegistryOps\$RegistryInfoLookup \
      net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext \
      net.minecraft.world.level.levelgen.structure.placement.StructurePlacement \
      net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement \
      net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement \
      net.minecraft.world.level.StructureManager \
      net.minecraft.server.level.ServerChunkCache \
      net.minecraft.world.level.chunk.ChunkAccess \
      net.minecraft.server.level.WorldGenRegion \
      net.minecraft.server.packs.PackLocationInfo \
      net.minecraft.world.level.storage.loot.LootDataType \
      net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement
    exit 0
  fi
done < <(if command -v rg >/dev/null; then
  rg --files . "$HOME/.gradle/caches" -g '*.jar'
else
  find . "$HOME/.gradle/caches" -name '*.jar' -type f
fi)
echo 'Paper worldgen classes not found' >&2
exit 1
