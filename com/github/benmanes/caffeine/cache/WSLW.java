package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

class WSLW<K, V> extends WSL<K, V> {
   static final LocalCacheFactory FACTORY = WSLW::new;
   final Ticker ticker;
   final WriteOrderDeque<Node<K, V>> writeOrderDeque;
   volatile long expiresAfterWriteNanos;
   final Pacer pacer;

   WSLW(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.ticker = var1.getTicker();
      this.writeOrderDeque = new WriteOrderDeque<>();
      this.expiresAfterWriteNanos = var1.getExpiresAfterWriteNanos();
      this.pacer = var1.getScheduler() == Scheduler.disabledScheduler() ? null : new Pacer(var1.getScheduler());
   }

   @Override
   public final Ticker expirationTicker() {
      return this.ticker;
   }

   @Override
   protected final WriteOrderDeque<Node<K, V>> writeOrderDeque() {
      return this.writeOrderDeque;
   }

   @Override
   protected final boolean expiresAfterWrite() {
      return true;
   }

   @Override
   protected final long expiresAfterWriteNanos() {
      return this.expiresAfterWriteNanos;
   }

   @Override
   protected final void setExpiresAfterWriteNanos(long var1) {
      this.expiresAfterWriteNanos = var1;
   }

   @Override
   public final Pacer pacer() {
      return this.pacer;
   }
}
