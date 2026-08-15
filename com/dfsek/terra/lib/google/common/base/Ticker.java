package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@GwtCompatible
public abstract class Ticker {
   private static final Ticker SYSTEM_TICKER = new Ticker() {
      @Override
      public long read() {
         return System.nanoTime();
      }
   };

   protected Ticker() {
   }

   public abstract long read();

   public static Ticker systemTicker() {
      return SYSTEM_TICKER;
   }
}
