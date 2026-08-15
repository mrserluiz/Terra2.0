package org.incendo.cloud.bukkit.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class CraftBukkitReflection {
   private static final String PREFIX_NMS = "net.minecraft.server";
   private static final String PREFIX_MC = "net.minecraft.";
   private static final String PREFIX_CRAFTBUKKIT = "org.bukkit.craftbukkit";
   private static final String CRAFT_SERVER = "CraftServer";
   private static final String CB_PKG_VERSION;
   public static final int MAJOR_REVISION;

   @SafeVarargs
   public static <T> @Nullable T firstNonNullOrNull(final @Nullable T @NonNull ... elements) {
      for (T element : elements) {
         if (element != null) {
            return element;
         }
      }

      return null;
   }

   @SafeVarargs
   public static <T> T firstNonNullOrThrow(final Supplier<String> errorMessage, final T... elements) {
      T t = firstNonNullOrNull(elements);
      if (t == null) {
         throw new IllegalArgumentException(errorMessage.get());
      } else {
         return t;
      }
   }

   public static @NonNull Class<?> needNMSClassOrElse(final @NonNull String nms, final @NonNull String... classNames) throws RuntimeException {
      Class<?> nmsClass = findNMSClass(nms);
      return nmsClass != null
         ? nmsClass
         : firstNonNullOrThrow(
            () -> String.format("Cound't find the NMS class '%s', or any of the following fallbacks: %s", nms, Arrays.toString(classNames)),
            Arrays.stream(classNames).map(CraftBukkitReflection::findClass).toArray(Class[]::new)
         );
   }

   public static @NonNull Class<?> needMCClass(final @NonNull String name) throws RuntimeException {
      return needClass("net.minecraft." + name);
   }

   public static @NonNull Class<?> needNMSClass(final @NonNull String className) throws RuntimeException {
      return needClass("net.minecraft.server" + CB_PKG_VERSION + className);
   }

   public static @NonNull Class<?> needOBCClass(final @NonNull String className) throws RuntimeException {
      return needClass("org.bukkit.craftbukkit" + CB_PKG_VERSION + className);
   }

   public static @Nullable Class<?> findMCClass(final @NonNull String name) throws RuntimeException {
      return findClass("net.minecraft." + name);
   }

   public static @Nullable Class<?> findNMSClass(final @NonNull String className) throws RuntimeException {
      return findClass("net.minecraft.server" + CB_PKG_VERSION + className);
   }

   public static @Nullable Class<?> findOBCClass(final @NonNull String className) throws RuntimeException {
      return findClass("org.bukkit.craftbukkit" + CB_PKG_VERSION + className);
   }

   public static @NonNull Class<?> needClass(final @NonNull String className) throws RuntimeException {
      try {
         return Class.forName(className);
      } catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
   }

   public static @Nullable Class<?> findClass(final @NonNull String className) {
      try {
         return Class.forName(className);
      } catch (ClassNotFoundException e) {
         return null;
      }
   }

   public static @NonNull Field needField(final @NonNull Class<?> holder, final @NonNull String name) throws RuntimeException {
      try {
         Field field = holder.getDeclaredField(name);
         field.setAccessible(true);
         return field;
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }
   }

   public static @Nullable Field findField(final @NonNull Class<?> holder, final @NonNull String name) throws RuntimeException {
      try {
         return needField(holder, name);
      } catch (RuntimeException e) {
         return null;
      }
   }

   public static @NonNull Constructor<?> needConstructor(final @NonNull Class<?> holder, final @NonNull Class<?>... parameters) {
      try {
         return holder.getDeclaredConstructor(parameters);
      } catch (NoSuchMethodException ex) {
         throw new RuntimeException(ex);
      }
   }

   public static @Nullable Constructor<?> findConstructor(final @NonNull Class<?> holder, final @NonNull Class<?>... parameters) {
      try {
         return holder.getDeclaredConstructor(parameters);
      } catch (NoSuchMethodException ex) {
         return null;
      }
   }

   public static boolean classExists(final @NonNull String className) {
      return findClass(className) != null;
   }

   public static @Nullable Method findMethod(final @NonNull Class<?> holder, final @NonNull String name, final @NonNull Class<?>... params) throws RuntimeException {
      try {
         return holder.getMethod(name, params);
      } catch (NoSuchMethodException e) {
         return null;
      }
   }

   public static @NonNull Method needMethod(final @NonNull Class<?> holder, final @NonNull String name, final @NonNull Class<?>... params) throws RuntimeException {
      try {
         return holder.getMethod(name, params);
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }

   public static Stream<Method> streamMethods(final @NonNull Class<?> clazz) {
      return Arrays.stream(clazz.getDeclaredMethods());
   }

   public static Object invokeConstructorOrStaticMethod(final Executable executable, final Object... args) throws ReflectiveOperationException {
      if (executable instanceof Constructor) {
         return ((Constructor)executable).newInstance(args);
      } else if (!Modifier.isStatic(executable.getModifiers())) {
         throw new IllegalArgumentException("Method " + executable + " is not static.");
      } else {
         return ((Method)executable).invoke(null, args);
      }
   }

   private CraftBukkitReflection() {
   }

   static {
      Class<?> serverClass;
      if (Bukkit.getServer() == null) {
         serverClass = needClass("org.bukkit.craftbukkit.CraftServer");
      } else {
         serverClass = Bukkit.getServer().getClass();
      }

      String pkg = serverClass.getPackage().getName();
      String nmsVersion = pkg.substring(pkg.lastIndexOf(".") + 1);
      if (!nmsVersion.contains("_")) {
         int fallbackVersion = -1;
         if (Bukkit.getServer() != null) {
            try {
               Method getMinecraftVersion = serverClass.getDeclaredMethod("getMinecraftVersion");
               fallbackVersion = Integer.parseInt(getMinecraftVersion.invoke(Bukkit.getServer()).toString().split("\\.")[1]);
            } catch (Exception var13) {
            }
         } else {
            try {
               Class<?> sharedConstants = needClass("net.minecraft.SharedConstants");
               Method getCurrentVersion = sharedConstants.getDeclaredMethod("getCurrentVersion");
               Object currentVersion = getCurrentVersion.invoke(null);
               Method getName = null;

               try {
                  getName = currentVersion.getClass().getDeclaredMethod("getName");
               } catch (NoSuchMethodException var11) {
               }

               if (getName == null) {
                  getName = currentVersion.getClass().getDeclaredMethod("name");
               }

               String versionName = (String)getName.invoke(currentVersion);

               try {
                  fallbackVersion = Integer.parseInt(versionName.split("\\.")[1]);
               } catch (Exception var10) {
               }
            } catch (ReflectiveOperationException e) {
               throw new RuntimeException(e);
            }
         }

         MAJOR_REVISION = fallbackVersion;
      } else {
         MAJOR_REVISION = Integer.parseInt(nmsVersion.split("_")[1]);
      }

      String name = serverClass.getName();
      name = name.substring("org.bukkit.craftbukkit".length());
      name = name.substring(0, name.length() - "CraftServer".length());
      CB_PKG_VERSION = name;
   }
}
