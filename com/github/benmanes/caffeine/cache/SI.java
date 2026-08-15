package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;
import org.jspecify.annotations.Nullable;

class SI<K, V> extends BoundedLocalCache<K, V> {
   static final LocalCacheFactory FACTORY = SI::new;
   final ReferenceQueue<V> valueReferenceQueue = new ReferenceQueue<>();

   SI(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, (AsyncCacheLoader<K, V>)var2, var3);
   }

   @Override
   protected final ReferenceQueue<V> valueReferenceQueue() {
      return this.valueReferenceQueue;
   }

   @Override
   protected final boolean collectValues() {
      return true;
   }
}
