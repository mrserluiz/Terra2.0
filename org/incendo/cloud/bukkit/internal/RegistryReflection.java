package org.incendo.cloud.bukkit.internal;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class RegistryReflection {
   public static final @Nullable Field REGISTRY_REGISTRY;
   public static final @Nullable Method REGISTRY_GET;
   public static final @Nullable Method REGISTRY_KEY;
   private static final Class<?> RESOURCE_LOCATION_CLASS = CraftBukkitReflection.needNMSClassOrElse(
      "MinecraftKey", "net.minecraft.resources.MinecraftKey", "net.minecraft.resources.ResourceLocation"
   );
   private static final Class<?> RESOURCE_KEY_CLASS = CraftBukkitReflection.needNMSClassOrElse("ResourceKey", "net.minecraft.resources.ResourceKey");
   private static final Executable NEW_RESOURCE_LOCATION;
   private static final Executable CREATE_REGISTRY_RESOURCE_KEY;

   private RegistryReflection() {
   }

   public static Object registryKey(final String registryName) {
      Objects.requireNonNull(CREATE_REGISTRY_RESOURCE_KEY, "CREATE_REGISTRY_RESOURCE_KEY");

      try {
         Object resourceLocation = createResourceLocation(registryName);
         return CraftBukkitReflection.invokeConstructorOrStaticMethod(CREATE_REGISTRY_RESOURCE_KEY, resourceLocation);
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }
   }

   public static Object get(final Object registry, final String resourceLocation) {
      Objects.requireNonNull(REGISTRY_GET, "REGISTRY_GET");

      try {
         return REGISTRY_GET.invoke(registry, createResourceLocation(resourceLocation));
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }
   }

   public static Object builtInRegistryByName(final String name) {
      Objects.requireNonNull(REGISTRY_REGISTRY, "REGISTRY_REGISTRY");

      try {
         return get(REGISTRY_REGISTRY.get(null), name);
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }
   }

   public static Object createResourceLocation(final String str) {
      try {
         return CraftBukkitReflection.invokeConstructorOrStaticMethod(NEW_RESOURCE_LOCATION, str);
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }
   }

   private static Field registryRegistryField(final Class<?> registryClass) {
      return Arrays.stream(registryClass.getDeclaredFields())
         .filter(it -> it.getType().equals(registryClass))
         .findFirst()
         .orElseGet(() -> registryRegistryFieldFromBuiltInRegistries(registryClass));
   }

   private static Field registryRegistryFieldFromBuiltInRegistries(final Class<?> registryClass) {
      Class<?> builtInRegistriesClass = CraftBukkitReflection.needMCClass("core.registries.BuiltInRegistries");
      return Arrays.stream(builtInRegistriesClass.getDeclaredFields()).filter(it -> {
         if (it.getType().equals(registryClass) && Modifier.isStatic(it.getModifiers())) {
            Type genericType = it.getGenericType();
            if (!(genericType instanceof ParameterizedType)) {
               return false;
            }

            Type valueType = ((ParameterizedType)genericType).getActualTypeArguments()[0];

            while (valueType instanceof WildcardType) {
               valueType = ((WildcardType)valueType).getUpperBounds()[0];
            }

            return GenericTypeReflector.erase(valueType).equals(registryClass);
         } else {
            return false;
         }
      }).findFirst().orElseThrow(() -> new IllegalStateException("Could not find Registry Registry field"));
   }

   static {
      if (CraftBukkitReflection.MAJOR_REVISION < 17) {
         REGISTRY_REGISTRY = null;
         REGISTRY_GET = null;
         REGISTRY_KEY = null;
         NEW_RESOURCE_LOCATION = null;
         CREATE_REGISTRY_RESOURCE_KEY = null;
      } else {
         Class<?> registryClass = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "Registry", CraftBukkitReflection.findMCClass("core.IRegistry"), CraftBukkitReflection.findMCClass("core.Registry")
         );
         REGISTRY_REGISTRY = registryRegistryField(registryClass);
         REGISTRY_REGISTRY.setAccessible(true);
         Class<?> resourceLocationClass = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "ResourceLocation class",
            CraftBukkitReflection.findMCClass("resources.ResourceLocation"),
            CraftBukkitReflection.findMCClass("resources.MinecraftKey")
         );
         REGISTRY_GET = Arrays.stream(registryClass.getDeclaredMethods())
            .filter(it -> it.getParameterCount() == 1 && it.getParameterTypes()[0].equals(resourceLocationClass) && it.getReturnType().equals(Object.class))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Could not find Registry#get(ResourceLocation)"));
         Class<?> resourceKeyClass = CraftBukkitReflection.needMCClass("resources.ResourceKey");
         REGISTRY_KEY = Arrays.stream(registryClass.getDeclaredMethods())
            .filter(m -> m.getParameterCount() == 0 && m.getReturnType().equals(resourceKeyClass))
            .findFirst()
            .orElse(null);
         NEW_RESOURCE_LOCATION = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "Could not find ResourceLocation#parse(String) or ResourceLocation#<init>(String)",
            CraftBukkitReflection.findConstructor(RESOURCE_LOCATION_CLASS, String.class),
            CraftBukkitReflection.findMethod(RESOURCE_LOCATION_CLASS, "parse", String.class),
            CraftBukkitReflection.findMethod(RESOURCE_LOCATION_CLASS, "a", String.class)
         );
         CREATE_REGISTRY_RESOURCE_KEY = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "Could not find ResourceKey#createRegistryKey(ResourceLocation)",
            CraftBukkitReflection.findMethod(RESOURCE_KEY_CLASS, "createRegistryKey", RESOURCE_LOCATION_CLASS),
            CraftBukkitReflection.findMethod(RESOURCE_KEY_CLASS, "a", RESOURCE_LOCATION_CLASS)
         );
      }
   }
}
