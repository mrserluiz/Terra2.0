package org.incendo.cloud.bukkit.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class CommandBuildContextSupplier {
   private static final Class<?> COMMAND_BUILD_CONTEXT_CLASS = CraftBukkitReflection.needMCClass("commands.CommandBuildContext");
   private static final @Nullable Constructor<?> COMMAND_BUILD_CONTEXT_CTR;
   private static final @Nullable Method CREATE_CONTEXT_METHOD;
   private static final @Nullable Method GET_WORLD_DATA_METHOD;
   private static final @Nullable Method GET_FEATURE_FLAGS_METHOD;
   private static final Class<?> REG_ACC_CLASS;
   private static final Class<?> MC_SERVER_CLASS = CraftBukkitReflection.needNMSClassOrElse("MinecraftServer", "net.minecraft.server.MinecraftServer");
   private static final Method GET_SERVER_METHOD;
   private static final Method REGISTRY_ACCESS;

   private CommandBuildContextSupplier() {
   }

   public static Object commandBuildContext() {
      if (COMMAND_BUILD_CONTEXT_CTR != null) {
         try {
            Object server = GET_SERVER_METHOD.invoke(null);
            return COMMAND_BUILD_CONTEXT_CTR.newInstance(REGISTRY_ACCESS.invoke(server));
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
         }
      } else if (CREATE_CONTEXT_METHOD != null && GET_WORLD_DATA_METHOD != null && GET_FEATURE_FLAGS_METHOD != null) {
         try {
            Object server = GET_SERVER_METHOD.invoke(null);
            Object worldData = GET_WORLD_DATA_METHOD.invoke(server);
            Object flags = GET_FEATURE_FLAGS_METHOD.invoke(worldData);
            return CREATE_CONTEXT_METHOD.invoke(null, REGISTRY_ACCESS.invoke(server), flags);
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
         }
      } else {
         throw new IllegalStateException();
      }
   }

   static {
      Constructor<?> ctr;
      try {
         ctr = COMMAND_BUILD_CONTEXT_CLASS.getDeclaredConstructors()[0];
      } catch (Exception ex) {
         ctr = null;
      }

      COMMAND_BUILD_CONTEXT_CTR = ctr;
      if (COMMAND_BUILD_CONTEXT_CTR == null) {
         List<Method> matchingFactoryMethods = Arrays.stream(COMMAND_BUILD_CONTEXT_CLASS.getDeclaredMethods())
            .filter(
               it -> it.getParameterCount() == 2 && COMMAND_BUILD_CONTEXT_CLASS.isAssignableFrom(it.getReturnType()) && Modifier.isStatic(it.getModifiers())
            )
            .collect(Collectors.toList());
         if (matchingFactoryMethods.size() == 1) {
            CREATE_CONTEXT_METHOD = matchingFactoryMethods.get(0);
         } else {
            if (matchingFactoryMethods.size() <= 1) {
               throw new IllegalStateException("Could not find CommandBuildContext factory method");
            }

            CREATE_CONTEXT_METHOD = matchingFactoryMethods.get(1);
         }

         Class<?> worldDataCls = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "Could not find WorldData class",
            CraftBukkitReflection.findMCClass("world.level.storage.SaveData"),
            CraftBukkitReflection.findMCClass("world.level.storage.WorldData")
         );
         GET_WORLD_DATA_METHOD = Arrays.stream(MC_SERVER_CLASS.getDeclaredMethods())
            .filter(it -> it.getParameterCount() == 0 && !Modifier.isStatic(it.getModifiers()) && it.getReturnType().equals(worldDataCls))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Could not find MinecraftServer#getWorldData method"));
         Class<?> featureFlagSetCls = CraftBukkitReflection.needMCClass("world.flag.FeatureFlagSet");
         GET_FEATURE_FLAGS_METHOD = Arrays.stream(worldDataCls.getDeclaredMethods())
            .filter(it -> it.getParameterCount() == 0 && it.getReturnType().equals(featureFlagSetCls) && !Modifier.isStatic(it.getModifiers()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Could not find enabledFeatures method"));
      } else {
         CREATE_CONTEXT_METHOD = null;
         GET_WORLD_DATA_METHOD = null;
         GET_FEATURE_FLAGS_METHOD = null;
      }

      REG_ACC_CLASS = COMMAND_BUILD_CONTEXT_CTR != null ? COMMAND_BUILD_CONTEXT_CTR.getParameterTypes()[0] : CREATE_CONTEXT_METHOD.getParameterTypes()[0];
      REGISTRY_ACCESS = Arrays.stream(MC_SERVER_CLASS.getDeclaredMethods())
         .filter(m -> REG_ACC_CLASS.isAssignableFrom(m.getReturnType()))
         .findFirst()
         .orElseThrow(() -> new IllegalStateException("Cannot find MinecraftServer#registryAccess"));

      try {
         GET_SERVER_METHOD = MC_SERVER_CLASS.getDeclaredMethod("getServer");
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }
}
