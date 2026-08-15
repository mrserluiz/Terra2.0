package com.github.benmanes.caffeine.cache;

enum SystemTicker implements Ticker {
   INSTANCE;

   @Override
   public long read() {
      return System.nanoTime();
   }
}
