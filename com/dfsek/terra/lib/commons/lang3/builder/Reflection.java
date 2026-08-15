package com.dfsek.terra.lib.commons.lang3.builder;

import java.lang.reflect.Field;
import java.util.Objects;

final class Reflection {
   static Object getUnchecked(Field field, Object obj) {
      try {
         return Objects.requireNonNull(field, "field").get(obj);
      } catch (IllegalAccessException e) {
         throw new IllegalArgumentException(e);
      }
   }
}
