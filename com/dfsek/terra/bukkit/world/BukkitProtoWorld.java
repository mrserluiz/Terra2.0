package com.dfsek.terra.bukkit.world;

import com.dfsek.terra.api.block.entity.BlockEntity;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.entity.Entity;
import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.api.world.ServerWorld;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.api.world.chunk.generation.ProtoWorld;
import com.dfsek.terra.bukkit.BukkitEntity;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import com.dfsek.terra.bukkit.util.BukkitUtils;
import com.dfsek.terra.bukkit.world.block.data.BukkitBlockState;
import com.dfsek.terra.bukkit.world.block.state.BukkitBlockEntity;
import com.dfsek.terra.bukkit.world.entity.BukkitEntityType;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BukkitProtoWorld implements ProtoWorld {
   private static final Logger LOGGER = LoggerFactory.getLogger(BukkitProtoWorld.class);
   private static final AtomicBoolean warn = new AtomicBoolean(true);
   private final LimitedRegion delegate;
   private final BlockState air;
   private final BiomeProvider biomeProvider;

   public BukkitProtoWorld(LimitedRegion delegate, BlockState air, BiomeProvider provider) {
      this.delegate = delegate;
      this.air = air;
      this.biomeProvider = provider;
   }

   public LimitedRegion getHandle() {
      return this.delegate;
   }

   @Override
   public void setBlockState(int x, int y, int z, BlockState data, boolean physics) {
      this.access(x, y, z, () -> {
         BlockData bukkitData = BukkitAdapter.adapt(data);
         this.delegate.setBlockData(x, y, z, bukkitData);
         if (physics) {
            if (BukkitUtils.isLiquid(bukkitData)) {
               this.delegate.scheduleFluidUpdate(x, y, z);
            } else {
               this.delegate.scheduleBlockUpdate(x, y, z);
            }
         }
      });
   }

   @Override
   public long getSeed() {
      return this.delegate.getWorld().getSeed();
   }

   @Override
   public int getMaxHeight() {
      return this.delegate.getWorld().getMaxHeight();
   }

   @Override
   public BlockState getBlockState(int x, int y, int z) {
      return this.<BlockState>access(x, y, z, () -> BukkitBlockState.newInstance(this.delegate.getBlockData(x, y, z))).orElse(this.air);
   }

   @Override
   public BlockEntity getBlockEntity(int x, int y, int z) {
      return this.<BukkitBlockEntity>access(x, y, z, () -> BukkitBlockEntity.newInstance(this.delegate.getBlockState(x, y, z))).orElse(null);
   }

   @Override
   public int getMinHeight() {
      return this.delegate.getWorld().getMinHeight();
   }

   @Override
   public Entity spawnEntity(double x, double y, double z, EntityType entityType) {
      return this.<BukkitEntity>access(
            (int)x,
            (int)y,
            (int)z,
            () -> new BukkitEntity(this.delegate.spawnEntity(new Location(this.delegate.getWorld(), x, y, z), ((BukkitEntityType)entityType).getHandle()))
         )
         .orElse(null);
   }

   @Override
   public ChunkGenerator getGenerator() {
      return ((BukkitChunkGeneratorWrapper)this.delegate.getWorld().getGenerator()).getHandle();
   }

   @Override
   public BiomeProvider getBiomeProvider() {
      return this.biomeProvider;
   }

   @Override
   public ConfigPack getPack() {
      return ((BukkitChunkGeneratorWrapper)this.delegate.getWorld().getGenerator()).getPack();
   }

   @Override
   public int centerChunkX() {
      return this.delegate.getCenterChunkX();
   }

   @Override
   public int centerChunkZ() {
      return this.delegate.getCenterChunkZ();
   }

   @Override
   public ServerWorld getWorld() {
      return new BukkitServerWorld(this.delegate.getWorld());
   }

   private <T> Optional<T> access(int x, int y, int z, Supplier<T> action) {
      if (this.delegate.isInRegion(x, y, z)) {
         return Optional.of(action.get());
      }

      if (warn.getAndSet(false)) {
         LOGGER.warn(
            "Detected world access at coordinates out of bounds: ({}, {}, {}) accessed for region [{}, {}]",
            new Object[]{x, y, z, this.delegate.getCenterChunkX(), this.delegate.getCenterChunkZ()}
         );
      } else {
         LOGGER.debug(
            "Detected world access at coordinates out of bounds: ({}, {}, {}) accessed for region [{}, {}]",
            new Object[]{x, y, z, this.delegate.getCenterChunkX(), this.delegate.getCenterChunkZ()}
         );
      }

      return Optional.empty();
   }

   private void access(int x, int y, int z, Runnable action) {
      if (this.delegate.isInRegion(x, y, z)) {
         action.run();
      } else if (warn.getAndSet(false)) {
         LOGGER.warn(
            "Detected world access at coordinates out of bounds: ({}, {}, {}) accessed for region [{}, {}]",
            new Object[]{x, y, z, this.delegate.getCenterChunkX(), this.delegate.getCenterChunkZ()}
         );
      } else {
         LOGGER.debug(
            "Detected world access at coordinates out of bounds: ({}, {}, {}) accessed for region [{}, {}]",
            new Object[]{x, y, z, this.delegate.getCenterChunkX(), this.delegate.getCenterChunkZ()}
         );
      }
   }
}
