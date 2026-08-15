package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

class SIA<K, V> extends SI<K, V> {
   static final LocalCacheFactory FACTORY = SIA::new;
   final Ticker ticker;
   final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque;
   final Expiry<K, V> expiry;
   final TimerWheel<K, V> timerWheel;
   volatile long expiresAfterAccessNanos;
   final Pacer pacer;

   SIA(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.ticker = var1.getTicker();
      this.accessOrderWindowDeque = !var1.evicts() && !var1.expiresAfterAccess() ? null : new AccessOrderDeque<>();
      this.expiry = var1.getExpiry(this.isAsync);
      this.timerWheel = var1.expiresVariable() ? new TimerWheel<>() : null;
      this.expiresAfterAccessNanos = var1.getExpiresAfterAccessNanos();
      this.pacer = var1.getScheduler() == Scheduler.disabledScheduler() ? null : new Pacer(var1.getScheduler());
   }

   @Override
   public final Ticker expirationTicker() {
      return this.ticker;
   }

   @Override
   protected final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque() {
      return this.accessOrderWindowDeque;
   }

   @Override
   protected final boolean expiresVariable() {
      return this.timerWheel != null;
   }

   @Override
   public final Expiry<K, V> expiry() {
      return this.expiry;
   }

   @Override
   protected final TimerWheel<K, V> timerWheel() {
      return this.timerWheel;
   }

   @Override
   protected final boolean expiresAfterAccess() {
      return this.timerWheel == null;
   }

   @Override
   protected final long expiresAfterAccessNanos() {
      return this.expiresAfterAccessNanos;
   }

   @Override
   protected final void setExpiresAfterAccessNanos(long var1) {
      this.expiresAfterAccessNanos = var1;
   }

   @Override
   public final Pacer pacer() {
      return this.pacer;
   }
}
