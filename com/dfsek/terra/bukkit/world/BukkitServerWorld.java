package com.dfsek.terra.bukkit.world;

import com.dfsek.terra.api.block.entity.BlockEntity;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.entity.Entity;
import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.api.world.ServerWorld;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.chunk.Chunk;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.bukkit.BukkitEntity;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import com.dfsek.terra.bukkit.world.block.state.BukkitBlockEntity;
import com.dfsek.terra.bukkit.world.entity.BukkitEntityType;
import org.bukkit.Location;
import org.bukkit.World;

public class BukkitServerWorld implements ServerWorld {
   private final World delegate;

   public BukkitServerWorld(World delegate) {
      this.delegate = delegate;
   }

   @Override
   public Entity spawnEntity(double x, double y, double z, EntityType entityType) {
      return new BukkitEntity(this.delegate.spawnEntity(new Location(this.delegate, x, y, z), ((BukkitEntityType)entityType).getHandle()));
   }

   @Override
   public void setBlockState(int x, int y, int z, BlockState data, boolean physics) {
      this.delegate.getBlockAt(x, y, z).setBlockData(BukkitAdapter.adapt(data), physics);
   }

   @Override
   public long getSeed() {
      return this.delegate.getSeed();
   }

   @Override
   public int getMaxHeight() {
      return this.delegate.getMaxHeight();
   }

   @Override
   public Chunk getChunkAt(int x, int z) {
      return BukkitAdapter.adapt(this.delegate.getChunkAt(x, z));
   }

   @Override
   public BlockState getBlockState(int x, int y, int z) {
      return BukkitAdapter.adapt(this.delegate.getBlockAt(x, y, z).getBlockData());
   }

   @Override
   public BlockEntity getBlockEntity(int x, int y, int z) {
      return BukkitBlockEntity.newInstance(this.delegate.getBlockAt(x, y, z).getState());
   }

   @Override
   public int getMinHeight() {
      return this.delegate.getMinHeight();
   }

   @Override
   public ChunkGenerator getGenerator() {
      return ((BukkitChunkGeneratorWrapper)this.delegate.getGenerator()).getHandle();
   }

   @Override
   public BiomeProvider getBiomeProvider() {
      return ((BukkitChunkGeneratorWrapper)this.delegate.getGenerator()).getPack().getBiomeProvider();
   }

   @Override
   public ConfigPack getPack() {
      return ((BukkitChunkGeneratorWrapper)this.delegate.getGenerator()).getPack();
   }

   public World getHandle() {
      return this.delegate;
   }

   @Override
   public int hashCode() {
      return this.delegate.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof BukkitServerWorld other ? other.getHandle().equals(this.delegate) : false;
   }

   @Override
   public String toString() {
      return this.delegate.toString();
   }
}
