package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@GwtCompatible
public final class Runnables {
   private static final Runnable EMPTY_RUNNABLE = () -> {};

   public static Runnable doNothing() {
      return EMPTY_RUNNABLE;
   }

   private Runnables() {
   }
}
