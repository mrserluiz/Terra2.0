package com.dfsek.terra.bukkit.generator;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.chunk.generation.ProtoChunk;
import com.dfsek.terra.bukkit.world.block.data.BukkitBlockState;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.jetbrains.annotations.NotNull;

public class BukkitProtoChunk implements ProtoChunk {
   private final ChunkData delegate;

   public BukkitProtoChunk(ChunkData delegate) {
      this.delegate = delegate;
   }

   public ChunkData getHandle() {
      return this.delegate;
   }

   @Override
   public int getMaxHeight() {
      return this.delegate.getMaxHeight();
   }

   @Override
   public void setBlock(int x, int y, int z, @NotNull BlockState blockState) {
      this.delegate.setBlock(x, y, z, ((BukkitBlockState)blockState).getHandle());
   }

   @NotNull
   @Override
   public BlockState getBlock(int x, int y, int z) {
      return BukkitBlockState.newInstance(this.delegate.getBlockData(x, y, z));
   }
}
