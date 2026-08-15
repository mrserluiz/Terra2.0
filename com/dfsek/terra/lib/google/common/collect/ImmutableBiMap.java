package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
public abstract class ImmutableBiMap<K, V> extends ImmutableMap<K, V> implements BiMap<K, V> {
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = -889275714L;

   public static <T, K, V> Collector<T, ?, ImmutableBiMap<K, V>> toImmutableBiMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      return CollectCollectors.toImmutableBiMap(keyFunction, valueFunction);
   }

   public static <K, V> ImmutableBiMap<K, V> of() {
      return (ImmutableBiMap<K, V>)RegularImmutableBiMap.EMPTY;
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1) {
      return new SingletonImmutableBiMap<>(k1, v1);
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2) {
      return RegularImmutableBiMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2));
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      return RegularImmutableBiMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3));
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return RegularImmutableBiMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4));
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return RegularImmutableBiMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5));
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
      return RegularImmutableBiMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5), entryOf(k6, v6));
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
      return RegularImmutableBiMap.fromEntries(
         entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5), entryOf(k6, v6), entryOf(k7, v7)
      );
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
      return RegularImmutableBiMap.fromEntries(
         entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5), entryOf(k6, v6), entryOf(k7, v7), entryOf(k8, v8)
      );
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
      return RegularImmutableBiMap.fromEntries(
         entryOf(k1, v1),
         entryOf(k2, v2),
         entryOf(k3, v3),
         entryOf(k4, v4),
         entryOf(k5, v5),
         entryOf(k6, v6),
         entryOf(k7, v7),
         entryOf(k8, v8),
         entryOf(k9, v9)
      );
   }

   public static <K, V> ImmutableBiMap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10
   ) {
      return RegularImmutableBiMap.fromEntries(
         entryOf(k1, v1),
         entryOf(k2, v2),
         entryOf(k3, v3),
         entryOf(k4, v4),
         entryOf(k5, v5),
         entryOf(k6, v6),
         entryOf(k7, v7),
         entryOf(k8, v8),
         entryOf(k9, v9),
         entryOf(k10, v10)
      );
   }

   @SafeVarargs
   public static <K, V> ImmutableBiMap<K, V> ofEntries(Entry<? extends K, ? extends V>... entries) {
      Entry<K, V>[] entries2 = (Entry<K, V>[])entries;
      return RegularImmutableBiMap.fromEntries(entries2);
   }

   public static <K, V> ImmutableBiMap.Builder<K, V> builder() {
      return new ImmutableBiMap.Builder<>();
   }

   public static <K, V> ImmutableBiMap.Builder<K, V> builderWithExpectedSize(int expectedSize) {
      CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
      return new ImmutableBiMap.Builder<>(expectedSize);
   }

   public static <K, V> ImmutableBiMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
      if (map instanceof ImmutableBiMap) {
         ImmutableBiMap<K, V> bimap = (ImmutableBiMap<K, V>)map;
         if (!bimap.isPartialView()) {
            return bimap;
         }
      }

      return copyOf(map.entrySet());
   }

   public static <K, V> ImmutableBiMap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      Entry<K, V>[] entryArray = Iterables.toArray((Iterable<? extends Entry<K, V>>)entries, (Entry<K, V>[])EMPTY_ENTRY_ARRAY);
      switch (entryArray.length) {
         case 0:
            return of();
         case 1:
            Entry<K, V> entry = entryArray[0];
            return of(entry.getKey(), entry.getValue());
         default:
            return RegularImmutableBiMap.fromEntries(entryArray);
      }
   }

   ImmutableBiMap() {
   }

   public abstract ImmutableBiMap<V, K> inverse();

   public ImmutableSet<V> values() {
      return this.inverse().keySet();
   }

   final ImmutableSet<V> createValues() {
      throw new AssertionError("should never be called");
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final @Nullable V forcePut(K key, V value) {
      throw new UnsupportedOperationException();
   }

   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return new ImmutableBiMap.SerializedForm<>(this);
   }

   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   @Deprecated
   @DoNotCall("Use toImmutableBiMap")
   public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Use toImmutableBiMap")
   public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction
   ) {
      throw new UnsupportedOperationException();
   }

   public static final class Builder<K, V> extends ImmutableMap.Builder<K, V> {
      public Builder() {
      }

      Builder(int size) {
         super(size);
      }

      @CanIgnoreReturnValue
      public ImmutableBiMap.Builder<K, V> put(K key, V value) {
         super.put(key, value);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableBiMap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         super.put(entry);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableBiMap.Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
         super.putAll(map);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableBiMap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         super.putAll(entries);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableBiMap.Builder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
         super.orderEntriesByValue(valueComparator);
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableBiMap.Builder<K, V> combine(ImmutableMap.Builder<K, V> builder) {
         super.combine(builder);
         return this;
      }

      public ImmutableBiMap<K, V> build() {
         return this.buildOrThrow();
      }

      public ImmutableBiMap<K, V> buildOrThrow() {
         switch (this.size) {
            case 0:
               return ImmutableBiMap.of();
            case 1:
               Entry<K, V> onlyEntry = Objects.requireNonNull(this.entries[0]);
               return ImmutableBiMap.of(onlyEntry.getKey(), onlyEntry.getValue());
            default:
               if (this.valueComparator != null) {
                  if (this.entriesUsed) {
                     this.entries = Arrays.copyOf(this.entries, this.size);
                  }

                  Arrays.sort(this.entries, 0, this.size, Ordering.from(this.valueComparator).onResultOf(Maps.valueFunction()));
               }

               this.entriesUsed = true;
               return RegularImmutableBiMap.fromEntryArray(this.size, this.entries);
         }
      }

      @Deprecated
      @DoNotCall
      public ImmutableBiMap<K, V> buildKeepingLast() {
         throw new UnsupportedOperationException("Not supported for bimaps");
      }

      @VisibleForTesting
      ImmutableBiMap<K, V> buildJdkBacked() {
         Preconditions.checkState(this.valueComparator == null, "buildJdkBacked is for tests only, doesn't support orderEntriesByValue");
         switch (this.size) {
            case 0:
               return ImmutableBiMap.of();
            case 1:
               Entry<K, V> onlyEntry = Objects.requireNonNull(this.entries[0]);
               return ImmutableBiMap.of(onlyEntry.getKey(), onlyEntry.getValue());
            default:
               this.entriesUsed = true;
               return RegularImmutableBiMap.fromEntryArray(this.size, this.entries);
         }
      }
   }

   @J2ktIncompatible
   private static class SerializedForm<K, V> extends ImmutableMap.SerializedForm<K, V> {
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      SerializedForm(ImmutableBiMap<K, V> bimap) {
         super(bimap);
      }

      ImmutableBiMap.Builder<K, V> makeBuilder(int size) {
         return new ImmutableBiMap.Builder<>(size);
      }
   }
}
