package com.github.benmanes.caffeine.cache;

import java.util.function.Consumer;

enum DisabledBuffer implements Buffer<Object> {
   INSTANCE;

   @Override
   public int offer(Object e) {
      return 0;
   }

   @Override
   public void drainTo(Consumer<Object> consumer) {
   }

   @Override
   public long size() {
      return 0L;
   }

   @Override
   public long reads() {
      return 0L;
   }

   @Override
   public long writes() {
      return 0L;
   }
}
