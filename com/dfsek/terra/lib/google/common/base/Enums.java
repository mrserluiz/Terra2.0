package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
@J2ktIncompatible
public final class Enums {
   @GwtIncompatible
   private static final Map<Class<? extends Enum<?>>, Map<String, WeakReference<? extends Enum<?>>>> enumConstantCache = new WeakHashMap<>();

   private Enums() {
   }

   @GwtIncompatible
   public static Field getField(Enum<?> enumValue) {
      Class<?> clazz = enumValue.getDeclaringClass();

      try {
         return clazz.getDeclaredField(enumValue.name());
      } catch (NoSuchFieldException impossible) {
         throw new AssertionError(impossible);
      }
   }

   public static <T extends Enum<T>> Optional<T> getIfPresent(Class<T> enumClass, String value) {
      Preconditions.checkNotNull(enumClass);
      Preconditions.checkNotNull(value);
      return Platform.getEnumIfPresent(enumClass, value);
   }

   @GwtIncompatible
   private static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> populateCache(Class<T> enumClass) {
      Map<String, WeakReference<? extends Enum<?>>> result = new HashMap<>();

      for (T enumInstance : EnumSet.allOf(enumClass)) {
         result.put(enumInstance.name(), new WeakReference<>(enumInstance));
      }

      enumConstantCache.put(enumClass, result);
      return result;
   }

   @GwtIncompatible
   static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> getEnumConstants(Class<T> enumClass) {
      synchronized (enumConstantCache) {
         Map<String, WeakReference<? extends Enum<?>>> constants = enumConstantCache.get(enumClass);
         if (constants == null) {
            constants = populateCache(enumClass);
         }

         return constants;
      }
   }

   @GwtIncompatible
   public static <T extends Enum<T>> Converter<String, T> stringConverter(Class<T> enumClass) {
      return new Enums.StringConverter<>(enumClass);
   }

   @GwtIncompatible
   private static final class StringConverter<T extends Enum<T>> extends Converter<String, T> implements Serializable {
      private final Class<T> enumClass;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      StringConverter(Class<T> enumClass) {
         this.enumClass = Preconditions.checkNotNull(enumClass);
      }

      protected T doForward(String value) {
         return Enum.valueOf(this.enumClass, value);
      }

      protected String doBackward(T enumValue) {
         return enumValue.name();
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (object instanceof Enums.StringConverter) {
            Enums.StringConverter<?> that = (Enums.StringConverter<?>)object;
            return this.enumClass.equals(that.enumClass);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.enumClass.hashCode();
      }

      @Override
      public String toString() {
         return "Enums.stringConverter(" + this.enumClass.getName() + ".class)";
      }
   }
}
