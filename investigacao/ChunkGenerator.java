package com.dfsek.terra.api.world.chunk.generation;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.util.vector.Vector3;
import com.dfsek.terra.api.util.vector.Vector3Int;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.chunk.generation.util.Palette;
import com.dfsek.terra.api.world.info.WorldProperties;
import org.jetbrains.annotations.NotNull;

public interface ChunkGenerator {
   void generateChunkData(@NotNull ProtoChunk var1, @NotNull WorldProperties var2, @NotNull BiomeProvider var3, int var4, int var5);

   BlockState getBlock(WorldProperties var1, int var2, int var3, int var4, BiomeProvider var5);

   default BlockState getBlock(WorldProperties world, Vector3 vector3, BiomeProvider biomeProvider) {
      return this.getBlock(world, vector3.getBlockX(), vector3.getBlockY(), vector3.getBlockZ(), biomeProvider);
   }

   default BlockState getBlock(WorldProperties world, Vector3Int vector3, BiomeProvider biomeProvider) {
      return this.getBlock(world, vector3.getX(), vector3.getY(), vector3.getZ(), biomeProvider);
   }

   Palette getPalette(int var1, int var2, int var3, WorldProperties var4, BiomeProvider var5);
}
