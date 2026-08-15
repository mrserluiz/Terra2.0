package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@GwtCompatible
abstract class CommonPattern {
   public abstract CommonMatcher matcher(CharSequence t);

   public abstract String pattern();

   public abstract int flags();

   @Override
   public abstract String toString();

   public static CommonPattern compile(String pattern) {
      return Platform.compilePattern(pattern);
   }

   public static boolean isPcreLike() {
      return Platform.patternCompilerIsPcreLike();
   }
}
