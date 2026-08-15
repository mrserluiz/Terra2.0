package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class NullnessCasts {
   @ParametricNullness
   static <T> T uncheckedCastNullableTToT(@Nullable T t) {
      return t;
   }

   private NullnessCasts() {
   }
}
