package com.dfsek.terra.bukkit.generator;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.world.chunk.generation.util.GeneratorWrapper;
import com.dfsek.terra.api.world.info.WorldProperties;
import com.dfsek.terra.bukkit.world.BukkitWorldProperties;
import java.util.List;
import java.util.Random;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BukkitChunkGeneratorWrapper extends ChunkGenerator implements GeneratorWrapper {
   private static final Logger LOGGER = LoggerFactory.getLogger(BukkitChunkGeneratorWrapper.class);
   private final BlockState air;
   private final BukkitBlockPopulator blockPopulator;
   private com.dfsek.terra.api.world.chunk.generation.ChunkGenerator delegate;
   private ConfigPack pack;

   public BukkitChunkGeneratorWrapper(com.dfsek.terra.api.world.chunk.generation.ChunkGenerator delegate, ConfigPack pack, BlockState air) {
      this.delegate = delegate;
      this.pack = pack;
      this.air = air;
      this.blockPopulator = new BukkitBlockPopulator(pack, air);
   }

   public void setDelegate(com.dfsek.terra.api.world.chunk.generation.ChunkGenerator delegate) {
      this.delegate = delegate;
   }

   @Nullable
   public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
      return new BukkitBiomeProvider(this.pack.getBiomeProvider());
   }

   public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkData chunkData) {
      BukkitWorldProperties properties = new BukkitWorldProperties(worldInfo);
      this.delegate.generateChunkData(new BukkitProtoChunk(chunkData), properties, this.pack.getBiomeProvider(), x, z);
   }

   @NotNull
   public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
      return List.of(this.blockPopulator);
   }

   public boolean shouldGenerateCaves() {
      return false;
   }

   public boolean shouldGenerateDecorations() {
      return true;
   }

   public boolean shouldGenerateMobs() {
      return true;
   }

   public boolean shouldGenerateStructures() {
      return true;
   }

   public ConfigPack getPack() {
      return this.pack;
   }

   public void setPack(ConfigPack pack) {
      this.pack = pack;
      this.setDelegate(pack.getGeneratorProvider().newInstance(pack));
   }

   @Override
   public com.dfsek.terra.api.world.chunk.generation.ChunkGenerator getHandle() {
      return this.delegate;
   }

   private record SeededVector(int x, int z, WorldProperties worldProperties) {
      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof BukkitChunkGeneratorWrapper.SeededVector that)
            ? false
            : this.z == that.z && this.x == that.x && this.worldProperties.equals(that.worldProperties);
      }

      @Override
      public int hashCode() {
         int code = this.x;
         code = 31 * code + this.z;
         return 31 * code + this.worldProperties.hashCode();
      }
   }
}
