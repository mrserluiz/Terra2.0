package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.Arrays;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Objects extends ExtraObjectsMethodsForWeb {
   private Objects() {
   }

   public static boolean equal(@Nullable Object a, @Nullable Object b) {
      return a == b || a != null && a.equals(b);
   }

   public static int hashCode(@Nullable Object @Nullable ... objects) {
      return Arrays.hashCode(objects);
   }
}
