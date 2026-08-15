package com.github.benmanes.caffeine.cache.stats;

import com.github.benmanes.caffeine.cache.RemovalCause;
import java.util.Objects;

enum DisabledStatsCounter implements StatsCounter {
   INSTANCE;

   @Override
   public void recordHits(int count) {
   }

   @Override
   public void recordMisses(int count) {
   }

   @Override
   public void recordLoadSuccess(long loadTime) {
   }

   @Override
   public void recordLoadFailure(long loadTime) {
   }

   @Override
   public void recordEviction(int weight, RemovalCause cause) {
      Objects.requireNonNull(cause);
   }

   @Override
   public CacheStats snapshot() {
      return CacheStats.empty();
   }

   @Override
   public String toString() {
      return this.snapshot().toString();
   }
}
