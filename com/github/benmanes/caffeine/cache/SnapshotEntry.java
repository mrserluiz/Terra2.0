package com.github.benmanes.caffeine.cache;

import com.google.errorprone.annotations.Immutable;
import java.util.Objects;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@Immutable(containerOf = {"K", "V"})
class SnapshotEntry<K, V> implements Policy.CacheEntry<K, V> {
   private final long snapshot;
   private final V value;
   private final K key;

   SnapshotEntry(K key, V value, long snapshot) {
      this.snapshot = snapshot;
      this.key = Objects.requireNonNull(key);
      this.value = Objects.requireNonNull(value);
   }

   @Override
   public final K getKey() {
      return this.key;
   }

   @Override
   public final V getValue() {
      return this.value;
   }

   @Override
   public V setValue(V value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int weight() {
      return 1;
   }

   @Override
   public long expiresAt() {
      return this.snapshot + Long.MAX_VALUE;
   }

   @Override
   public long refreshableAt() {
      return this.snapshot + Long.MAX_VALUE;
   }

   @Override
   public final long snapshotAt() {
      return this.snapshot;
   }

   @Override
   public final boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof Entry)) {
         return false;
      }

      Entry<?, ?> entry = (Entry<?, ?>)o;
      return this.key.equals(entry.getKey()) && this.value.equals(entry.getValue());
   }

   @Override
   public final int hashCode() {
      return this.key.hashCode() ^ this.value.hashCode();
   }

   @Override
   public final String toString() {
      return this.key + "=" + this.value;
   }

   public static <K, V> SnapshotEntry<K, V> forEntry(K key, V value) {
      return new SnapshotEntry<>(key, value, 0L);
   }

   public static <K, V> SnapshotEntry<K, V> forEntry(K key, V value, long snapshot, int weight, long expiresAt, long refreshableAt) {
      long unsetTicks = snapshot + Long.MAX_VALUE;
      boolean refresh = refreshableAt != unsetTicks;
      boolean expires = expiresAt != unsetTicks;
      boolean weights = weight != 1;
      int features = (weights ? 1 : 0) | (expires ? 2 : 0) | (refresh ? 4 : 0);
      switch (features) {
         case 0:
            return new SnapshotEntry<>(key, value, snapshot);
         case 1:
            return new SnapshotEntry.WeightedEntry<>(key, value, snapshot, weight);
         case 2:
            return new SnapshotEntry.ExpirableEntry<>(key, value, snapshot, expiresAt);
         case 3:
            return new SnapshotEntry.ExpirableWeightedEntry<>(key, value, snapshot, weight, expiresAt);
         case 4:
         case 5:
         default:
            return new SnapshotEntry.CompleteEntry<>(key, value, snapshot, weight, expiresAt, refreshableAt);
         case 6:
            return new SnapshotEntry.RefreshableExpirableEntry<>(key, value, snapshot, expiresAt, refreshableAt);
      }
   }

   static final class CompleteEntry<K, V> extends SnapshotEntry.ExpirableWeightedEntry<K, V> {
      final long refreshableAt;

      CompleteEntry(K key, V value, long snapshot, int weight, long expiresAt, long refreshableAt) {
         super(key, value, snapshot, weight, expiresAt);
         this.refreshableAt = refreshableAt;
      }

      @Override
      public long refreshableAt() {
         return this.refreshableAt;
      }
   }

   static class ExpirableEntry<K, V> extends SnapshotEntry<K, V> {
      final long expiresAt;

      ExpirableEntry(K key, V value, long snapshot, long expiresAt) {
         super(key, value, snapshot);
         this.expiresAt = expiresAt;
      }

      @Override
      public final long expiresAt() {
         return this.expiresAt;
      }
   }

   static class ExpirableWeightedEntry<K, V> extends SnapshotEntry.WeightedEntry<K, V> {
      final long expiresAt;

      ExpirableWeightedEntry(K key, V value, long snapshot, int weight, long expiresAt) {
         super(key, value, snapshot, weight);
         this.expiresAt = expiresAt;
      }

      @Override
      public final long expiresAt() {
         return this.expiresAt;
      }
   }

   static class RefreshableExpirableEntry<K, V> extends SnapshotEntry.ExpirableEntry<K, V> {
      final long refreshableAt;

      RefreshableExpirableEntry(K key, V value, long snapshot, long expiresAt, long refreshableAt) {
         super(key, value, snapshot, expiresAt);
         this.refreshableAt = refreshableAt;
      }

      @Override
      public final long refreshableAt() {
         return this.refreshableAt;
      }
   }

   static class WeightedEntry<K, V> extends SnapshotEntry<K, V> {
      final int weight;

      WeightedEntry(K key, V value, long snapshot, int weight) {
         super(key, value, snapshot);
         this.weight = weight;
      }

      @Override
      public final int weight() {
         return this.weight;
      }
   }
}
