package com.dfsek.terra.api.world;

import com.dfsek.terra.api.block.entity.BlockEntity;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.entity.Entity;
import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.api.util.vector.Vector3Int;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.api.world.util.Interceptors;
import com.dfsek.terra.api.world.util.ReadInterceptor;
import com.dfsek.terra.api.world.util.WriteInterceptor;
import java.util.Objects;

public class BufferedWorld implements WritableWorld {
   private final WritableWorld delegate;
   private final int offsetX;
   private final int offsetY;
   private final int offsetZ;
   private final ReadInterceptor readInterceptor;
   private final WriteInterceptor writeInterceptor;

   protected BufferedWorld(WritableWorld delegate, int offsetX, int offsetY, int offsetZ, ReadInterceptor readInterceptor, WriteInterceptor writeInterceptor) {
      this.delegate = delegate;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      this.readInterceptor = readInterceptor;
      this.writeInterceptor = writeInterceptor;
   }

   protected static BufferedWorld.Builder builder(WritableWorld world) {
      return new BufferedWorld.Builder(world);
   }

   @Override
   public Object getHandle() {
      return this.delegate.getHandle();
   }

   @Override
   public BlockState getBlockState(int x, int y, int z) {
      return this.readInterceptor.read(x + this.offsetX, y + this.offsetY, z + this.offsetZ, this.delegate);
   }

   @Override
   public BlockEntity getBlockEntity(int x, int y, int z) {
      return this.delegate.getBlockEntity(x + this.offsetX, y + this.offsetY, z + this.offsetZ);
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
   public int getMinHeight() {
      return this.delegate.getMinHeight();
   }

   @Override
   public ChunkGenerator getGenerator() {
      return this.delegate.getGenerator();
   }

   @Override
   public BiomeProvider getBiomeProvider() {
      return this.delegate.getBiomeProvider();
   }

   @Override
   public ConfigPack getPack() {
      return this.delegate.getPack();
   }

   @Override
   public void setBlockState(int x, int y, int z, BlockState data, boolean physics) {
      this.writeInterceptor.write(x + this.offsetX, y + this.offsetY, z + this.offsetZ, data, this.delegate, physics);
   }

   @Override
   public Entity spawnEntity(double x, double y, double z, EntityType entityType) {
      return this.delegate.spawnEntity(x + this.offsetX, y + this.offsetY, z + this.offsetZ, entityType);
   }

   public WritableWorld getDelegate() {
      return this.delegate;
   }

   public static final class Builder {
      private final WritableWorld delegate;
      private ReadInterceptor readInterceptor;
      private WriteInterceptor writeInterceptor;
      private int x = 0;
      private int y = 0;
      private int z = 0;

      private Builder(WritableWorld delegate) {
         this.delegate = delegate;
      }

      public BufferedWorld.Builder read(ReadInterceptor interceptor) {
         this.readInterceptor = interceptor;
         return this;
      }

      public BufferedWorld.Builder write(WriteInterceptor interceptor) {
         this.writeInterceptor = interceptor;
         return this;
      }

      public BufferedWorld.Builder offsetX(int x) {
         this.x = x;
         return this;
      }

      public BufferedWorld.Builder offsetY(int y) {
         this.y = y;
         return this;
      }

      public BufferedWorld.Builder offsetZ(int z) {
         this.z = z;
         return this;
      }

      public BufferedWorld.Builder offset(Vector3Int vector) {
         this.x = vector.getX();
         this.y = vector.getY();
         this.z = vector.getZ();
         return this;
      }

      public BufferedWorld build() {
         return new BufferedWorld(
            this.delegate,
            this.x,
            this.y,
            this.z,
            Objects.requireNonNullElse(this.readInterceptor, Interceptors.readThrough()),
            Objects.requireNonNullElse(this.writeInterceptor, Interceptors.writeThrough())
         );
      }
   }
}
