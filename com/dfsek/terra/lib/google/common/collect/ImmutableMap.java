package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.DoNotMock;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use ImmutableMap.of or another implementation")
@GwtCompatible(serializable = true, emulated = true)
public abstract class ImmutableMap<K, V> implements Map<K, V>, Serializable {
   static final Entry<?, ?>[] EMPTY_ENTRY_ARRAY = new Entry[0];
   @LazyInit
   @RetainedWith
   private transient @Nullable ImmutableSet<Entry<K, V>> entrySet;
   @LazyInit
   @RetainedWith
   private transient @Nullable ImmutableSet<K> keySet;
   @LazyInit
   @RetainedWith
   private transient @Nullable ImmutableCollection<V> values;
   @LazyInit
   private transient @Nullable ImmutableSetMultimap<K, V> multimapView;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = -889275714L;

   public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      return CollectCollectors.toImmutableMap(keyFunction, valueFunction);
   }

   public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction
   ) {
      return CollectCollectors.toImmutableMap(keyFunction, valueFunction, mergeFunction);
   }

   public static <K, V> ImmutableMap<K, V> of() {
      return (ImmutableMap<K, V>)RegularImmutableMap.EMPTY;
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1) {
      return ImmutableBiMap.of(k1, v1);
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
      return RegularImmutableMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2));
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      return RegularImmutableMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3));
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return RegularImmutableMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4));
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return RegularImmutableMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5));
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
      return RegularImmutableMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5), entryOf(k6, v6));
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
      return RegularImmutableMap.fromEntries(
         entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5), entryOf(k6, v6), entryOf(k7, v7)
      );
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
      return RegularImmutableMap.fromEntries(
         entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5), entryOf(k6, v6), entryOf(k7, v7), entryOf(k8, v8)
      );
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
      return RegularImmutableMap.fromEntries(
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

   public static <K, V> ImmutableMap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10
   ) {
      return RegularImmutableMap.fromEntries(
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
   public static <K, V> ImmutableMap<K, V> ofEntries(Entry<? extends K, ? extends V>... entries) {
      Entry<K, V>[] entries2 = (Entry<K, V>[])entries;
      return RegularImmutableMap.fromEntries(entries2);
   }

   static <K, V> Entry<K, V> entryOf(K key, V value) {
      return new ImmutableMapEntry<>(key, value);
   }

   public static <K, V> ImmutableMap.Builder<K, V> builder() {
      return new ImmutableMap.Builder<>();
   }

   public static <K, V> ImmutableMap.Builder<K, V> builderWithExpectedSize(int expectedSize) {
      CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
      return new ImmutableMap.Builder<>(expectedSize);
   }

   static void checkNoConflict(boolean safe, String conflictDescription, Object entry1, Object entry2) {
      if (!safe) {
         throw conflictException(conflictDescription, entry1, entry2);
      }
   }

   static IllegalArgumentException conflictException(String conflictDescription, Object entry1, Object entry2) {
      return new IllegalArgumentException("Multiple entries with same " + conflictDescription + ": " + entry1 + " and " + entry2);
   }

   public static <K, V> ImmutableMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
      if (map instanceof ImmutableMap && !(map instanceof SortedMap)) {
         ImmutableMap<K, V> kvMap = (ImmutableMap<K, V>)map;
         if (!kvMap.isPartialView()) {
            return kvMap;
         }
      } else if (map instanceof EnumMap) {
         return (ImmutableMap<K, V>)copyOfEnumMap((EnumMap<?, ? extends V>)map);
      }

      return copyOf(map.entrySet());
   }

   public static <K, V> ImmutableMap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      Entry<K, V>[] entryArray = Iterables.toArray((Iterable<? extends Entry<K, V>>)entries, (Entry<K, V>[])EMPTY_ENTRY_ARRAY);
      switch (entryArray.length) {
         case 0:
            return of();
         case 1:
            Entry<K, V> onlyEntry = Objects.requireNonNull(entryArray[0]);
            return of(onlyEntry.getKey(), onlyEntry.getValue());
         default:
            return RegularImmutableMap.fromEntries(entryArray);
      }
   }

   private static <K extends Enum<K>, V> ImmutableMap<K, ? extends V> copyOfEnumMap(EnumMap<?, ? extends V> original) {
      EnumMap<K, V> copy = new EnumMap<>((EnumMap<K, ? extends V>)original);

      for (Entry<K, V> entry : copy.entrySet()) {
         CollectPreconditions.checkEntryNotNull(entry.getKey(), entry.getValue());
      }

      return ImmutableEnumMap.asImmutable(copy);
   }

   ImmutableMap() {
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final @Nullable V put(K k, V v) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final @Nullable V putIfAbsent(K key, V value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean replace(K key, V oldValue, V newValue) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final @Nullable V replace(K key, V value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final @Nullable V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final @Nullable V compute(K key, BiFunction<? super K, ? super @Nullable V, ? extends @Nullable V> remappingFunction) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final @Nullable V merge(K key, V value, BiFunction<? super V, ? super V, ? extends @Nullable V> function) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final void putAll(Map<? extends K, ? extends V> map) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final @Nullable V remove(@Nullable Object o) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean remove(@Nullable Object key, @Nullable Object value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final void clear() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public boolean containsKey(@Nullable Object key) {
      return this.get(key) != null;
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      return this.values().contains(value);
   }

   @Override
   public abstract @Nullable V get(@Nullable Object key);

   @Override
   public final @Nullable V getOrDefault(@Nullable Object key, @Nullable V defaultValue) {
      V result = this.get(key);
      return result != null ? result : defaultValue;
   }

   public ImmutableSet<Entry<K, V>> entrySet() {
      ImmutableSet<Entry<K, V>> result = this.entrySet;
      return result == null ? (this.entrySet = this.createEntrySet()) : result;
   }

   abstract ImmutableSet<Entry<K, V>> createEntrySet();

   public ImmutableSet<K> keySet() {
      ImmutableSet<K> result = this.keySet;
      return result == null ? (this.keySet = this.createKeySet()) : result;
   }

   abstract ImmutableSet<K> createKeySet();

   UnmodifiableIterator<K> keyIterator() {
      final UnmodifiableIterator<Entry<K, V>> entryIterator = this.entrySet().iterator();
      return new UnmodifiableIterator<K>() {
         @Override
         public boolean hasNext() {
            return entryIterator.hasNext();
         }

         @Override
         public K next() {
            return entryIterator.next().getKey();
         }
      };
   }

   Spliterator<K> keySpliterator() {
      return CollectSpliterators.map(this.entrySet().spliterator(), Entry::getKey);
   }

   public ImmutableCollection<V> values() {
      ImmutableCollection<V> result = this.values;
      return result == null ? (this.values = this.createValues()) : result;
   }

   abstract ImmutableCollection<V> createValues();

   public ImmutableSetMultimap<K, V> asMultimap() {
      if (this.isEmpty()) {
         return ImmutableSetMultimap.of();
      }

      ImmutableSetMultimap<K, V> result = this.multimapView;
      return result == null ? (this.multimapView = new ImmutableSetMultimap<>(new ImmutableMap.MapViewOfValuesAsSingletonSets(), this.size(), null)) : result;
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return Maps.equalsImpl(this, object);
   }

   abstract boolean isPartialView();

   @Override
   public int hashCode() {
      return Sets.hashCodeImpl(this.entrySet());
   }

   boolean isHashCodeFast() {
      return false;
   }

   @Override
   public String toString() {
      return Maps.toStringImpl(this);
   }

   @J2ktIncompatible
   Object writeReplace() {
      return new ImmutableMap.SerializedForm<>(this);
   }

   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   @DoNotMock
   public static class Builder<K, V> {
      @Nullable Comparator<? super V> valueComparator;
      @Nullable Entry<K, V>[] entries;
      int size;
      boolean entriesUsed;

      public Builder() {
         this(4);
      }

      Builder(int initialCapacity) {
         this.entries = new Entry[initialCapacity];
         this.size = 0;
         this.entriesUsed = false;
      }

      private void ensureCapacity(int minCapacity) {
         if (minCapacity > this.entries.length) {
            this.entries = Arrays.copyOf(this.entries, ImmutableCollection.Builder.expandedCapacity(this.entries.length, minCapacity));
            this.entriesUsed = false;
         }
      }

      @CanIgnoreReturnValue
      public ImmutableMap.Builder<K, V> put(K key, V value) {
         this.ensureCapacity(this.size + 1);
         Entry<K, V> entry = ImmutableMap.entryOf(key, value);
         this.entries[this.size++] = entry;
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         return this.put((K)entry.getKey(), (V)entry.getValue());
      }

      @CanIgnoreReturnValue
      public ImmutableMap.Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
         return this.putAll(map.entrySet());
      }

      @CanIgnoreReturnValue
      public ImmutableMap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         if (entries instanceof Collection) {
            this.ensureCapacity(this.size + ((Collection)entries).size());
         }

         for (Entry<? extends K, ? extends V> entry : entries) {
            this.put(entry);
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMap.Builder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
         Preconditions.checkState(this.valueComparator == null, "valueComparator was already set");
         this.valueComparator = Preconditions.checkNotNull(valueComparator, "valueComparator");
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableMap.Builder<K, V> combine(ImmutableMap.Builder<K, V> other) {
         Preconditions.checkNotNull(other);
         this.ensureCapacity(this.size + other.size);
         System.arraycopy(other.entries, 0, this.entries, this.size, other.size);
         this.size = this.size + other.size;
         return this;
      }

      private ImmutableMap<K, V> build(boolean throwIfDuplicateKeys) {
         switch (this.size) {
            case 0:
               return ImmutableMap.of();
            case 1:
               Entry<K, V> onlyEntry = Objects.requireNonNull(this.entries[0]);
               return ImmutableMap.of(onlyEntry.getKey(), onlyEntry.getValue());
            default:
               int localSize = this.size;
               Entry<K, V>[] localEntries;
               if (this.valueComparator == null) {
                  localEntries = this.entries;
               } else {
                  if (this.entriesUsed) {
                     this.entries = Arrays.copyOf(this.entries, this.size);
                  }

                  Entry<K, V>[] nonNullEntries = this.entries;
                  if (!throwIfDuplicateKeys) {
                     Entry<K, V>[] lastEntryForEachKey = lastEntryForEachKey(nonNullEntries, this.size);
                     if (lastEntryForEachKey != null) {
                        nonNullEntries = lastEntryForEachKey;
                        localSize = lastEntryForEachKey.length;
                     }
                  }

                  Arrays.sort(nonNullEntries, 0, localSize, Ordering.from(this.valueComparator).onResultOf(Maps.valueFunction()));
                  localEntries = nonNullEntries;
               }

               this.entriesUsed = true;
               return RegularImmutableMap.fromEntryArray(localSize, localEntries, throwIfDuplicateKeys);
         }
      }

      public ImmutableMap<K, V> build() {
         return this.buildOrThrow();
      }

      public ImmutableMap<K, V> buildOrThrow() {
         return this.build(true);
      }

      public ImmutableMap<K, V> buildKeepingLast() {
         return this.build(false);
      }

      @VisibleForTesting
      ImmutableMap<K, V> buildJdkBacked() {
         Preconditions.checkState(this.valueComparator == null, "buildJdkBacked is only for testing; can't use valueComparator");
         switch (this.size) {
            case 0:
               return ImmutableMap.of();
            case 1:
               Entry<K, V> onlyEntry = Objects.requireNonNull(this.entries[0]);
               return ImmutableMap.of(onlyEntry.getKey(), onlyEntry.getValue());
            default:
               this.entriesUsed = true;
               return JdkBackedImmutableMap.create(this.size, this.entries, true);
         }
      }

      private static <K, V> Entry<K, V> @Nullable [] lastEntryForEachKey(Entry<K, V>[] entries, int size) {
         Set<K> seen = new HashSet<>();
         BitSet dups = new BitSet();

         for (int i = size - 1; i >= 0; i--) {
            if (!seen.add(entries[i].getKey())) {
               dups.set(i);
            }
         }

         if (dups.isEmpty()) {
            return null;
         }

         Entry<K, V>[] newEntries = new Entry[size - dups.cardinality()];
         int inI = 0;
         int outI = 0;

         while (inI < size) {
            if (!dups.get(inI)) {
               newEntries[outI++] = entries[inI];
            }

            inI++;
         }

         return newEntries;
      }
   }

   abstract static class IteratorBasedImmutableMap<K, V> extends ImmutableMap<K, V> {
      abstract UnmodifiableIterator<Entry<K, V>> entryIterator();

      Spliterator<Entry<K, V>> entrySpliterator() {
         return Spliterators.spliterator(this.entryIterator(), this.size(), 1297);
      }

      @Override
      ImmutableSet<K> createKeySet() {
         return new ImmutableMapKeySet<>(this);
      }

      @Override
      ImmutableSet<Entry<K, V>> createEntrySet() {
         class EntrySetImpl extends ImmutableMapEntrySet<K, V> {
            @Override
            ImmutableMap<K, V> map() {
               return IteratorBasedImmutableMap.this;
            }

            @Override
            public UnmodifiableIterator<Entry<K, V>> iterator() {
               return IteratorBasedImmutableMap.this.entryIterator();
            }

            @J2ktIncompatible
            @GwtIncompatible
            @Override
            Object writeReplace() {
               return super.writeReplace();
            }
         }

         return new EntrySetImpl();
      }

      @Override
      ImmutableCollection<V> createValues() {
         return new ImmutableMapValues<>(this);
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }

   private final class MapViewOfValuesAsSingletonSets extends ImmutableMap.IteratorBasedImmutableMap<K, ImmutableSet<V>> {
      private MapViewOfValuesAsSingletonSets() {
      }

      @Override
      public int size() {
         return ImmutableMap.this.size();
      }

      @Override
      ImmutableSet<K> createKeySet() {
         return ImmutableMap.this.keySet();
      }

      @Override
      public boolean containsKey(@Nullable Object key) {
         return ImmutableMap.this.containsKey(key);
      }

      public @Nullable ImmutableSet<V> get(@Nullable Object key) {
         V outerValue = ImmutableMap.this.get(key);
         return outerValue == null ? null : ImmutableSet.of(outerValue);
      }

      @Override
      boolean isPartialView() {
         return ImmutableMap.this.isPartialView();
      }

      @Override
      public int hashCode() {
         return ImmutableMap.this.hashCode();
      }

      @Override
      boolean isHashCodeFast() {
         return ImmutableMap.this.isHashCodeFast();
      }

      @Override
      UnmodifiableIterator<Entry<K, ImmutableSet<V>>> entryIterator() {
         final Iterator<Entry<K, V>> backingIterator = ImmutableMap.this.entrySet().iterator();
         return new UnmodifiableIterator<Entry<K, ImmutableSet<V>>>() {
            @Override
            public boolean hasNext() {
               return backingIterator.hasNext();
            }

            public Entry<K, ImmutableSet<V>> next() {
               final Entry<K, V> backingEntry = backingIterator.next();
               return new AbstractMapEntry<K, ImmutableSet<V>>() {
                  @Override
                  public K getKey() {
                     return backingEntry.getKey();
                  }

                  public ImmutableSet<V> getValue() {
                     return ImmutableSet.of(backingEntry.getValue());
                  }
               };
            }
         };
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }

   @J2ktIncompatible
   static class SerializedForm<K, V> implements Serializable {
      private static final boolean USE_LEGACY_SERIALIZATION = true;
      private final Object keys;
      private final Object values;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      SerializedForm(ImmutableMap<K, V> map) {
         Object[] keys = new Object[map.size()];
         Object[] values = new Object[map.size()];
         int i = 0;

         for (Entry<? extends Object, ? extends Object> entry : map.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
         }

         this.keys = keys;
         this.values = values;
      }

      final Object readResolve() {
         if (!(this.keys instanceof ImmutableSet)) {
            return this.legacyReadResolve();
         }

         ImmutableSet<K> keySet = (ImmutableSet<K>)this.keys;
         ImmutableCollection<V> values = (ImmutableCollection<V>)this.values;
         ImmutableMap.Builder<K, V> builder = this.makeBuilder(keySet.size());
         UnmodifiableIterator<K> keyIter = keySet.iterator();
         UnmodifiableIterator<V> valueIter = values.iterator();

         while (keyIter.hasNext()) {
            builder.put(keyIter.next(), valueIter.next());
         }

         return builder.buildOrThrow();
      }

      final Object legacyReadResolve() {
         K[] keys = (K[])((Object[])this.keys);
         V[] values = (V[])((Object[])this.values);
         ImmutableMap.Builder<K, V> builder = this.makeBuilder(keys.length);

         for (int i = 0; i < keys.length; i++) {
            builder.put(keys[i], values[i]);
         }

         return builder.buildOrThrow();
      }

      ImmutableMap.Builder<K, V> makeBuilder(int size) {
         return new ImmutableMap.Builder<>(size);
      }
   }
}
