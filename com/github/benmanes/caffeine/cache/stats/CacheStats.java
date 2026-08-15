package com.github.benmanes.caffeine.cache.stats;

import com.google.errorprone.annotations.Immutable;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Immutable
@NullMarked
public final class CacheStats {
   private static final CacheStats EMPTY_STATS = of(0L, 0L, 0L, 0L, 0L, 0L, 0L);
   private final long hitCount;
   private final long missCount;
   private final long loadSuccessCount;
   private final long loadFailureCount;
   private final long totalLoadTime;
   private final long evictionCount;
   private final long evictionWeight;

   private CacheStats(long hitCount, long missCount, long loadSuccessCount, long loadFailureCount, long totalLoadTime, long evictionCount, long evictionWeight) {
      if (hitCount >= 0L
         && missCount >= 0L
         && loadSuccessCount >= 0L
         && loadFailureCount >= 0L
         && totalLoadTime >= 0L
         && evictionCount >= 0L
         && evictionWeight >= 0L) {
         this.hitCount = hitCount;
         this.missCount = missCount;
         this.loadSuccessCount = loadSuccessCount;
         this.loadFailureCount = loadFailureCount;
         this.totalLoadTime = totalLoadTime;
         this.evictionCount = evictionCount;
         this.evictionWeight = evictionWeight;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public static CacheStats of(
      long hitCount, long missCount, long loadSuccessCount, long loadFailureCount, long totalLoadTime, long evictionCount, long evictionWeight
   ) {
      return new CacheStats(hitCount, missCount, loadSuccessCount, loadFailureCount, totalLoadTime, evictionCount, evictionWeight);
   }

   public static CacheStats empty() {
      return EMPTY_STATS;
   }

   public long requestCount() {
      return saturatedAdd(this.hitCount, this.missCount);
   }

   public long hitCount() {
      return this.hitCount;
   }

   public double hitRate() {
      long requestCount = this.requestCount();
      return requestCount == 0L ? 1.0 : (double)this.hitCount / requestCount;
   }

   public long missCount() {
      return this.missCount;
   }

   public double missRate() {
      long requestCount = this.requestCount();
      return requestCount == 0L ? 0.0 : (double)this.missCount / requestCount;
   }

   public long loadCount() {
      return saturatedAdd(this.loadSuccessCount, this.loadFailureCount);
   }

   public long loadSuccessCount() {
      return this.loadSuccessCount;
   }

   public long loadFailureCount() {
      return this.loadFailureCount;
   }

   public double loadFailureRate() {
      long totalLoadCount = saturatedAdd(this.loadSuccessCount, this.loadFailureCount);
      return totalLoadCount == 0L ? 0.0 : (double)this.loadFailureCount / totalLoadCount;
   }

   public long totalLoadTime() {
      return this.totalLoadTime;
   }

   public double averageLoadPenalty() {
      long totalLoadCount = saturatedAdd(this.loadSuccessCount, this.loadFailureCount);
      return totalLoadCount == 0L ? 0.0 : (double)this.totalLoadTime / totalLoadCount;
   }

   public long evictionCount() {
      return this.evictionCount;
   }

   public long evictionWeight() {
      return this.evictionWeight;
   }

   public CacheStats minus(CacheStats other) {
      return of(
         Math.max(0L, this.hitCount - other.hitCount),
         Math.max(0L, this.missCount - other.missCount),
         Math.max(0L, this.loadSuccessCount - other.loadSuccessCount),
         Math.max(0L, this.loadFailureCount - other.loadFailureCount),
         Math.max(0L, this.totalLoadTime - other.totalLoadTime),
         Math.max(0L, this.evictionCount - other.evictionCount),
         Math.max(0L, this.evictionWeight - other.evictionWeight)
      );
   }

   public CacheStats plus(CacheStats other) {
      return of(
         saturatedAdd(this.hitCount, other.hitCount),
         saturatedAdd(this.missCount, other.missCount),
         saturatedAdd(this.loadSuccessCount, other.loadSuccessCount),
         saturatedAdd(this.loadFailureCount, other.loadFailureCount),
         saturatedAdd(this.totalLoadTime, other.totalLoadTime),
         saturatedAdd(this.evictionCount, other.evictionCount),
         saturatedAdd(this.evictionWeight, other.evictionWeight)
      );
   }

   private static long saturatedAdd(long a, long b) {
      long naiveSum = a + b;
      return (a ^ b) < 0L | (a ^ naiveSum) >= 0L ? naiveSum : Long.MAX_VALUE + (naiveSum >>> 63 ^ 1L);
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.hitCount, this.missCount, this.loadSuccessCount, this.loadFailureCount, this.totalLoadTime, this.evictionCount, this.evictionWeight
      );
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof CacheStats)) {
         return false;
      }

      CacheStats other = (CacheStats)o;
      return this.hitCount == other.hitCount
         && this.missCount == other.missCount
         && this.loadSuccessCount == other.loadSuccessCount
         && this.loadFailureCount == other.loadFailureCount
         && this.totalLoadTime == other.totalLoadTime
         && this.evictionCount == other.evictionCount
         && this.evictionWeight == other.evictionWeight;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName()
         + "{hitCount="
         + this.hitCount
         + ", missCount="
         + this.missCount
         + ", loadSuccessCount="
         + this.loadSuccessCount
         + ", loadFailureCount="
         + this.loadFailureCount
         + ", totalLoadTime="
         + this.totalLoadTime
         + ", evictionCount="
         + this.evictionCount
         + ", evictionWeight="
         + this.evictionWeight
         + "}";
   }
}
