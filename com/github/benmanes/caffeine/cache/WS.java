package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;
import org.jspecify.annotations.Nullable;

class WS<K, V> extends BoundedLocalCache<K, V> {
   static final LocalCacheFactory FACTORY = WS::new;
   final ReferenceQueue<K> keyReferenceQueue = new ReferenceQueue<>();

   WS(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, (AsyncCacheLoader<K, V>)var2, var3);
   }

   @Override
   protected final ReferenceQueue<K> keyReferenceQueue() {
      return this.keyReferenceQueue;
   }

   @Override
   protected final boolean collectKeys() {
      return true;
   }
}
