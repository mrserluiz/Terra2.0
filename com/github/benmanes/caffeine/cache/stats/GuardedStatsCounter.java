package com.github.benmanes.caffeine.cache.stats;

import com.github.benmanes.caffeine.cache.RemovalCause;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;

final class GuardedStatsCounter implements StatsCounter {
   static final Logger logger = System.getLogger(GuardedStatsCounter.class.getName());
   final StatsCounter delegate;

   GuardedStatsCounter(StatsCounter delegate) {
      this.delegate = Objects.requireNonNull(delegate);
   }

   @Override
   public void recordHits(int count) {
      try {
         this.delegate.recordHits(count);
      } catch (Throwable t) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", t);
      }
   }

   @Override
   public void recordMisses(int count) {
      try {
         this.delegate.recordMisses(count);
      } catch (Throwable t) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", t);
      }
   }

   @Override
   public void recordLoadSuccess(long loadTime) {
      try {
         this.delegate.recordLoadSuccess(loadTime);
      } catch (Throwable t) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", t);
      }
   }

   @Override
   public void recordLoadFailure(long loadTime) {
      try {
         this.delegate.recordLoadFailure(loadTime);
      } catch (Throwable t) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", t);
      }
   }

   @Override
   public void recordEviction(int weight, RemovalCause cause) {
      Objects.requireNonNull(cause);

      try {
         this.delegate.recordEviction(weight, cause);
      } catch (Throwable t) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", t);
      }
   }

   @Override
   public CacheStats snapshot() {
      try {
         return this.delegate.snapshot();
      } catch (Throwable t) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", t);
         return CacheStats.empty();
      }
   }

   @Override
   public String toString() {
      return this.delegate.toString();
   }
}
