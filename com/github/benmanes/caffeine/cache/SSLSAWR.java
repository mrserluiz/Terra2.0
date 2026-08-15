package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

final class SSLSAWR<K, V> extends SSLSAW<K, V> {
   static final LocalCacheFactory FACTORY = SSLSAWR::new;
   volatile long refreshAfterWriteNanos;

   SSLSAWR(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.refreshAfterWriteNanos = var1.getRefreshAfterWriteNanos();
   }

   @Override
   protected boolean refreshAfterWrite() {
      return true;
   }

   @Override
   protected long refreshAfterWriteNanos() {
      return this.refreshAfterWriteNanos;
   }

   @Override
   protected void setRefreshAfterWriteNanos(long var1) {
      this.refreshAfterWriteNanos = var1;
   }
}
