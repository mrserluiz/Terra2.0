package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class Defaults {
   private static final Double DOUBLE_DEFAULT = 0.0;
   private static final Float FLOAT_DEFAULT = 0.0F;

   private Defaults() {
   }

   public static <T> @Nullable T defaultValue(Class<T> type) {
      Preconditions.checkNotNull(type);
      if (type.isPrimitive()) {
         if (type == boolean.class) {
            return (T)Boolean.FALSE;
         }

         if (type == char.class) {
            return (T)'\u0000';
         }

         if (type == byte.class) {
            return (T)(byte)0;
         }

         if (type == short.class) {
            return (T)(short)0;
         }

         if (type == int.class) {
            return (T)0;
         }

         if (type == long.class) {
            return (T)0L;
         }

         if (type == float.class) {
            return (T)FLOAT_DEFAULT;
         }

         if (type == double.class) {
            return (T)DOUBLE_DEFAULT;
         }
      }

      return null;
   }
}
