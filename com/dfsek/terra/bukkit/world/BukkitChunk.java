package com.dfsek.terra.bukkit.world;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.ServerWorld;
import com.dfsek.terra.api.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class BukkitChunk implements Chunk {
   private final org.bukkit.Chunk delegate;

   public BukkitChunk(org.bukkit.Chunk delegate) {
      this.delegate = delegate;
   }

   public org.bukkit.Chunk getHandle() {
      return this.delegate;
   }

   @Override
   public void setBlock(int x, int y, int z, BlockState data, boolean physics) {
      this.delegate.getBlock(x, y, z).setBlockData(BukkitAdapter.adapt(data), physics);
   }

   @Override
   public void setBlock(int x, int y, int z, @NotNull BlockState blockState) {
      this.delegate.getBlock(x, y, z).setBlockData(BukkitAdapter.adapt(blockState));
   }

   @NotNull
   @Override
   public BlockState getBlock(int x, int y, int z) {
      return BukkitAdapter.adapt(this.delegate.getBlock(x, y, z).getBlockData());
   }

   @Override
   public int getX() {
      return this.delegate.getX();
   }

   @Override
   public int getZ() {
      return this.delegate.getZ();
   }

   @Override
   public ServerWorld getWorld() {
      return BukkitAdapter.adapt(this.delegate.getWorld());
   }
}
