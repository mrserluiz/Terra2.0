package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

class SSL<K, V> extends SS<K, V> {
   static final LocalCacheFactory FACTORY = SSL::new;
   final RemovalListener<K, V> removalListener;

   SSL(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.removalListener = var1.getRemovalListener(var3);
   }

   @Override
   public final RemovalListener<K, V> removalListener() {
      return this.removalListener;
   }

   @Override
   protected final boolean hasRemovalListener() {
      return true;
   }
}
