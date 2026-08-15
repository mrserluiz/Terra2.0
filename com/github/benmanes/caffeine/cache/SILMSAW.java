package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

class SILMSAW<K, V> extends SILMSA<K, V> {
   static final LocalCacheFactory FACTORY = SILMSAW::new;
   final WriteOrderDeque<Node<K, V>> writeOrderDeque = new WriteOrderDeque<>();
   volatile long expiresAfterWriteNanos;

   SILMSAW(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.expiresAfterWriteNanos = var1.getExpiresAfterWriteNanos();
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
}
