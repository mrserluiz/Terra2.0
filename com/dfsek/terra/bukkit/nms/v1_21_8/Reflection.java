package com.dfsek.terra.bukkit.nms.v1_21_8;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;
import xyz.jpenilla.reflectionremapper.proxy.ReflectionProxyFactory;
import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldGetter;
import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldSetter;
import xyz.jpenilla.reflectionremapper.proxy.annotation.MethodName;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Static;

public class Reflection {
   public static final Reflection.MappedRegistryProxy MAPPED_REGISTRY;
   public static final Reflection.MappedRegistryTagSetProxy MAPPED_REGISTRY_TAG_SET;
   public static final Reflection.StructureManagerProxy STRUCTURE_MANAGER;
   public static final Reflection.ReferenceProxy REFERENCE;
   public static final Reflection.ChunkMapProxy CHUNKMAP;
   public static final Reflection.HolderReferenceProxy HOLDER_REFERENCE;
   public static final Reflection.HolderSetNamedProxy HOLDER_SET;
   public static final Reflection.BiomeProxy BIOME;
   public static final Reflection.VillagerTypeProxy VILLAGER_TYPE;

   static {
      ReflectionRemapper reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar();
      ReflectionProxyFactory reflectionProxyFactory = ReflectionProxyFactory.create(reflectionRemapper, Reflection.class.getClassLoader());
      MAPPED_REGISTRY = reflectionProxyFactory.reflectionProxy(Reflection.MappedRegistryProxy.class);
      MAPPED_REGISTRY_TAG_SET = reflectionProxyFactory.reflectionProxy(Reflection.MappedRegistryTagSetProxy.class);
      STRUCTURE_MANAGER = reflectionProxyFactory.reflectionProxy(Reflection.StructureManagerProxy.class);
      REFERENCE = reflectionProxyFactory.reflectionProxy(Reflection.ReferenceProxy.class);
      CHUNKMAP = reflectionProxyFactory.reflectionProxy(Reflection.ChunkMapProxy.class);
      HOLDER_REFERENCE = reflectionProxyFactory.reflectionProxy(Reflection.HolderReferenceProxy.class);
      HOLDER_SET = reflectionProxyFactory.reflectionProxy(Reflection.HolderSetNamedProxy.class);
      BIOME = reflectionProxyFactory.reflectionProxy(Reflection.BiomeProxy.class);
      VILLAGER_TYPE = reflectionProxyFactory.reflectionProxy(Reflection.VillagerTypeProxy.class);
   }

   @Proxies(Biome.class)
   public interface BiomeProxy {
      @MethodName("getGrassColorFromTexture")
      int invokeGrassColorFromTexture(Biome var1);
   }

   @Proxies(ChunkMap.class)
   public interface ChunkMapProxy {
      @FieldGetter("worldGenContext")
      WorldGenContext getWorldGenContext(ChunkMap var1);

      @FieldSetter("worldGenContext")
      void setWorldGenContext(ChunkMap var1, WorldGenContext var2);
   }

   @Proxies(Reference.class)
   public interface HolderReferenceProxy {
      @MethodName("bindTags")
      <T> void invokeBindTags(Reference<T> var1, Collection<TagKey<T>> var2);
   }

   @Proxies(Named.class)
   public interface HolderSetNamedProxy {
      @MethodName("bind")
      <T> void invokeBind(Named<T> var1, List<Holder<T>> var2);

      @MethodName("contents")
      <T> List<Holder<T>> invokeContents(Named<T> var1);
   }

   @Proxies(MappedRegistry.class)
   public interface MappedRegistryProxy {
      @FieldGetter("byKey")
      <T> Map<ResourceKey<T>, Reference<T>> getByKey(MappedRegistry<T> var1);

      @FieldSetter("allTags")
      <T> void setAllTags(MappedRegistry<T> var1, Object var2);

      @FieldSetter("frozen")
      void setFrozen(MappedRegistry<?> var1, boolean var2);

      @MethodName("createTag")
      <T> Named<T> invokeCreateTag(MappedRegistry<T> var1, TagKey<T> var2);
   }

   @Proxies(className = "net.minecraft.core.MappedRegistry$TagSet")
   public interface MappedRegistryTagSetProxy {
      @MethodName("fromMap")
      @Static
      <T> Object invokeFromMap(Map<TagKey<T>, Named<T>> var1);
   }

   @Proxies(Reference.class)
   public interface ReferenceProxy {
      @MethodName("bindValue")
      <T> void invokeBindValue(Reference<T> var1, T var2);

      @MethodName("bindTags")
      <T> void invokeBindTags(Reference<T> var1, Collection<TagKey<T>> var2);
   }

   @Proxies(StructureManager.class)
   public interface StructureManagerProxy {
      @FieldGetter("level")
      LevelAccessor getLevel(StructureManager var1);
   }

   @Proxies(VillagerType.class)
   public interface VillagerTypeProxy {
      @Static
      @FieldGetter("BY_BIOME")
      Map<ResourceKey<Biome>, ResourceKey<VillagerType>> getByBiome();
   }
}
