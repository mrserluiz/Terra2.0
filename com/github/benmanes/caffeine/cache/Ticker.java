package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface Ticker {
   long read();

   static Ticker systemTicker() {
      return SystemTicker.INSTANCE;
   }

   static Ticker disabledTicker() {
      return DisabledTicker.INSTANCE;
   }
}
