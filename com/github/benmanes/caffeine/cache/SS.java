package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

class SS<K, V> extends BoundedLocalCache<K, V> {
   static final LocalCacheFactory FACTORY = SS::new;

   SS(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, (AsyncCacheLoader<K, V>)var2, var3);
   }
}
