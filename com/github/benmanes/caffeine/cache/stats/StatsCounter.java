package com.github.benmanes.caffeine.cache.stats;

import com.github.benmanes.caffeine.cache.RemovalCause;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StatsCounter {
   void recordHits(int count);

   void recordMisses(int count);

   void recordLoadSuccess(long loadTime);

   void recordLoadFailure(long loadTime);

   void recordEviction(int weight, RemovalCause cause);

   CacheStats snapshot();

   static StatsCounter disabledStatsCounter() {
      return DisabledStatsCounter.INSTANCE;
   }

   static StatsCounter guardedStatsCounter(StatsCounter statsCounter) {
      return statsCounter instanceof GuardedStatsCounter ? statsCounter : new GuardedStatsCounter(statsCounter);
   }
}
