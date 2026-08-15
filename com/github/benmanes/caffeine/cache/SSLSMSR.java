package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

final class SSLSMSR<K, V> extends SSLSMS<K, V> {
   static final LocalCacheFactory FACTORY = SSLSMSR::new;
   final Ticker ticker;
   volatile long refreshAfterWriteNanos;

   SSLSMSR(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.ticker = var1.getTicker();
      this.refreshAfterWriteNanos = var1.getRefreshAfterWriteNanos();
   }

   @Override
   public Ticker expirationTicker() {
      return this.ticker;
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
