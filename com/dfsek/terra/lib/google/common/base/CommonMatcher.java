package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@GwtCompatible
abstract class CommonMatcher {
   public abstract boolean matches();

   public abstract boolean find();

   public abstract boolean find(int index);

   public abstract String replaceAll(String replacement);

   public abstract int end();

   public abstract int start();
}
