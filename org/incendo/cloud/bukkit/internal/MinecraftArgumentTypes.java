package org.incendo.cloud.bukkit.internal;

import com.dfsek.terra.lib.google.common.base.Suppliers;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class MinecraftArgumentTypes {
   private static final MinecraftArgumentTypes.ArgumentTypeGetter ARGUMENT_TYPE_GETTER;

   private MinecraftArgumentTypes() {
   }

   public static Class<? extends ArgumentType<?>> getClassByKey(final @NonNull NamespacedKey key) throws IllegalArgumentException {
      return ARGUMENT_TYPE_GETTER.getClassByKey(key);
   }

   static {
      if (CraftBukkitReflection.classExists("org.bukkit.entity.Warden")) {
         ARGUMENT_TYPE_GETTER = new MinecraftArgumentTypes.ArgumentTypeGetterImpl();
      } else {
         ARGUMENT_TYPE_GETTER = new MinecraftArgumentTypes.LegacyArgumentTypeGetter();
      }
   }

   private interface ArgumentTypeGetter {
      Class<? extends ArgumentType<?>> getClassByKey(@NonNull NamespacedKey key) throws IllegalArgumentException;
   }

   private static final class ArgumentTypeGetterImpl implements MinecraftArgumentTypes.ArgumentTypeGetter {
      private final Supplier<Object> argumentRegistry = Suppliers.memoize(() -> RegistryReflection.builtInRegistryByName("command_argument_type"));
      private final Map<?, ?> byClassMap;

      private ArgumentTypeGetterImpl() {
         try {
            Field declaredField = CraftBukkitReflection.needMCClass("commands.synchronization.ArgumentTypeInfos").getDeclaredFields()[0];
            declaredField.setAccessible(true);
            this.byClassMap = (Map<?, ?>)declaredField.get(null);
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
         }
      }

      @Override
      public Class<? extends ArgumentType<?>> getClassByKey(final @NonNull NamespacedKey key) throws IllegalArgumentException {
         Object argTypeInfo = RegistryReflection.get(this.argumentRegistry.get(), key.getNamespace() + ":" + key.getKey());

         for (Entry<?, ?> entry : this.byClassMap.entrySet()) {
            if (entry.getValue() == argTypeInfo) {
               return (Class<? extends ArgumentType<?>>)entry.getKey();
            }
         }

         throw new IllegalArgumentException(key.toString());
      }
   }

   private static final class LegacyArgumentTypeGetter implements MinecraftArgumentTypes.ArgumentTypeGetter {
      private static final Constructor<?> MINECRAFT_KEY_CONSTRUCTOR;
      private static final Method ARGUMENT_REGISTRY_GET_BY_KEY_METHOD;
      private static final Field BY_CLASS_MAP_FIELD;

      private LegacyArgumentTypeGetter() {
      }

      @Override
      public Class<? extends ArgumentType<?>> getClassByKey(final @NonNull NamespacedKey key) throws IllegalArgumentException {
         try {
            Object minecraftKey = MINECRAFT_KEY_CONSTRUCTOR.newInstance(key.getNamespace(), key.getKey());
            Object entry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.invoke(null, minecraftKey);
            if (entry == null) {
               throw new IllegalArgumentException(key.toString());
            }

            Map<Class<?>, Object> map = (Map<Class<?>, Object>)BY_CLASS_MAP_FIELD.get(null);

            for (Entry<Class<?>, Object> mapEntry : map.entrySet()) {
               if (mapEntry.getValue() == entry) {
                  return (Class<? extends ArgumentType<?>>)mapEntry.getKey();
               }
            }

            throw new IllegalArgumentException(key.toString());
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
         }
      }

      static {
         try {
            Class<?> minecraftKey;
            Class<?> argumentRegistry;
            if (CraftBukkitReflection.findMCClass("resources.ResourceLocation") != null) {
               minecraftKey = CraftBukkitReflection.needMCClass("resources.ResourceLocation");
               argumentRegistry = CraftBukkitReflection.needMCClass("commands.synchronization.ArgumentTypes");
            } else {
               minecraftKey = CraftBukkitReflection.needNMSClassOrElse("MinecraftKey", "net.minecraft.resources.MinecraftKey");
               argumentRegistry = CraftBukkitReflection.needNMSClassOrElse("ArgumentRegistry", "net.minecraft.commands.synchronization.ArgumentRegistry");
            }

            MINECRAFT_KEY_CONSTRUCTOR = minecraftKey.getConstructor(String.class, String.class);
            MINECRAFT_KEY_CONSTRUCTOR.setAccessible(true);
            ARGUMENT_REGISTRY_GET_BY_KEY_METHOD = Arrays.stream(argumentRegistry.getDeclaredMethods())
               .filter(method -> method.getParameterCount() == 1)
               .filter(method -> minecraftKey.equals(method.getParameterTypes()[0]))
               .findFirst()
               .orElseThrow(NoSuchMethodException::new);
            ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.setAccessible(true);
            BY_CLASS_MAP_FIELD = Arrays.stream(argumentRegistry.getDeclaredFields())
               .filter(field -> Modifier.isStatic(field.getModifiers()))
               .filter(field -> field.getType().equals(Map.class))
               .filter(field -> {
                  ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
                  Type param = parameterizedType.getActualTypeArguments()[0];
                  return !(param instanceof ParameterizedType) ? false : ((ParameterizedType)param).getRawType().equals(Class.class);
               })
               .findFirst()
               .orElseThrow(NoSuchFieldException::new);
            BY_CLASS_MAP_FIELD.setAccessible(true);
         } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
         }
      }
   }
}
