package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.MoreObjects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import com.google.j2objc.annotations.Weak;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
public class ImmutableSetMultimap<K, V> extends ImmutableMultimap<K, V> implements SetMultimap<K, V> {
   private final transient ImmutableSet<V> emptySet;
   @LazyInit
   @RetainedWith
   private transient @Nullable ImmutableSetMultimap<V, K> inverse;
   @LazyInit
   @RetainedWith
   private transient @Nullable ImmutableSet<Entry<K, V>> entries;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> toImmutableSetMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      return CollectCollectors.toImmutableSetMultimap(keyFunction, valueFunction);
   }

   public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> flatteningToImmutableSetMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends Stream<? extends V>> valuesFunction
   ) {
      return CollectCollectors.flatteningToImmutableSetMultimap(keyFunction, valuesFunction);
   }

   public static <K, V> ImmutableSetMultimap<K, V> of() {
      return EmptyImmutableSetMultimap.INSTANCE;
   }

   public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1) {
      ImmutableSetMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      return builder.build();
   }

   public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2) {
      ImmutableSetMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      return builder.build();
   }

   public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      ImmutableSetMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      builder.put(k3, v3);
      return builder.build();
   }

   public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      ImmutableSetMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      builder.put(k3, v3);
      builder.put(k4, v4);
      return builder.build();
   }

   public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      ImmutableSetMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      builder.put(k3, v3);
      builder.put(k4, v4);
      builder.put(k5, v5);
      return builder.build();
   }

   public static <K, V> ImmutableSetMultimap.Builder<K, V> builder() {
      return new ImmutableSetMultimap.Builder<>();
   }

   public static <K, V> ImmutableSetMultimap.Builder<K, V> builderWithExpectedKeys(int expectedKeys) {
      CollectPreconditions.checkNonnegative(expectedKeys, "expectedKeys");
      return new ImmutableSetMultimap.Builder<>(expectedKeys);
   }

   public static <K, V> ImmutableSetMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap) {
      return copyOf(multimap, null);
   }

   private static <K, V> ImmutableSetMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap, @Nullable Comparator<? super V> valueComparator) {
      Preconditions.checkNotNull(multimap);
      if (multimap.isEmpty() && valueComparator == null) {
         return of();
      }

      if (multimap instanceof ImmutableSetMultimap) {
         ImmutableSetMultimap<K, V> kvMultimap = (ImmutableSetMultimap<K, V>)multimap;
         if (!kvMultimap.isPartialView()) {
            return kvMultimap;
         }
      }

      return fromMapEntries(multimap.asMap().entrySet(), valueComparator);
   }

   public static <K, V> ImmutableSetMultimap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      return new ImmutableSetMultimap.Builder<K, V>().putAll(entries).build();
   }

   static <K, V> ImmutableSetMultimap<K, V> fromMapEntries(
      Collection<? extends Entry<? extends K, ? extends Collection<? extends V>>> mapEntries, @Nullable Comparator<? super V> valueComparator
   ) {
      if (mapEntries.isEmpty()) {
         return of();
      }

      ImmutableMap.Builder<K, ImmutableSet<V>> builder = new ImmutableMap.Builder<>(mapEntries.size());
      int size = 0;

      for (Entry<? extends K, ? extends Collection<? extends V>> entry : mapEntries) {
         K key = (K)entry.getKey();
         Collection<? extends V> values = (Collection<? extends V>)entry.getValue();
         ImmutableSet<V> set = valueSet(valueComparator, values);
         if (!set.isEmpty()) {
            builder.put(key, set);
            size += set.size();
         }
      }

      return new ImmutableSetMultimap<>(builder.buildOrThrow(), size, valueComparator);
   }

   static <K, V> ImmutableSetMultimap<K, V> fromMapBuilderEntries(
      Collection<? extends Entry<K, ImmutableCollection.Builder<V>>> mapEntries, @Nullable Comparator<? super V> valueComparator
   ) {
      if (mapEntries.isEmpty()) {
         return of();
      }

      ImmutableMap.Builder<K, ImmutableSet<V>> builder = new ImmutableMap.Builder<>(mapEntries.size());
      int size = 0;

      for (Entry<K, ImmutableCollection.Builder<V>> entry : mapEntries) {
         K key = entry.getKey();
         ImmutableSet.Builder<? extends V> values = (ImmutableSet.Builder<? extends V>)entry.getValue();
         ImmutableSet<V> set = valueSet(valueComparator, values.build());
         if (!set.isEmpty()) {
            builder.put(key, set);
            size += set.size();
         }
      }

      return new ImmutableSetMultimap<>(builder.buildOrThrow(), size, valueComparator);
   }

   ImmutableSetMultimap(ImmutableMap<K, ImmutableSet<V>> map, int size, @Nullable Comparator<? super V> valueComparator) {
      super(map, size);
      this.emptySet = emptySet(valueComparator);
   }

   public ImmutableSet<V> get(K key) {
      ImmutableSet<V> set = (ImmutableSet<V>)this.map.get(key);
      return MoreObjects.firstNonNull(set, this.emptySet);
   }

   public ImmutableSetMultimap<V, K> inverse() {
      ImmutableSetMultimap<V, K> result = this.inverse;
      return result == null ? (this.inverse = this.invert()) : result;
   }

   private ImmutableSetMultimap<V, K> invert() {
      ImmutableSetMultimap.Builder<V, K> builder = builder();

      for (Entry<K, V> entry : this.entries()) {
         builder.put(entry.getValue(), entry.getKey());
      }

      ImmutableSetMultimap<V, K> invertedMultimap = builder.build();
      invertedMultimap.inverse = this;
      return invertedMultimap;
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   public final ImmutableSet<V> removeAll(@Nullable Object key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   public final ImmutableSet<V> replaceValues(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
   }

   public ImmutableSet<Entry<K, V>> entries() {
      ImmutableSet<Entry<K, V>> result = this.entries;
      return result == null ? (this.entries = new ImmutableSetMultimap.EntrySet<>(this)) : result;
   }

   private static <V> ImmutableSet<V> valueSet(@Nullable Comparator<? super V> valueComparator, Collection<? extends V> values) {
      return valueComparator == null ? ImmutableSet.copyOf(values) : ImmutableSortedSet.copyOf(valueComparator, values);
   }

   private static <V> ImmutableSet<V> emptySet(@Nullable Comparator<? super V> valueComparator) {
      return valueComparator == null ? ImmutableSet.of() : ImmutableSortedSet.emptySet(valueComparator);
   }

   private static <V> ImmutableSet.Builder<V> valuesBuilder(@Nullable Comparator<? super V> valueComparator) {
      return valueComparator == null ? new ImmutableSet.Builder<>() : new ImmutableSortedSet.Builder<>(valueComparator);
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(this.valueComparator());
      Serialization.writeMultimap(this, stream);
   }

   @Nullable Comparator<? super V> valueComparator() {
      return this.emptySet instanceof ImmutableSortedSet ? ((ImmutableSortedSet)this.emptySet).comparator() : null;
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      Comparator<Object> valueComparator = (Comparator<Object>)stream.readObject();
      int keyCount = stream.readInt();
      if (keyCount < 0) {
         throw new InvalidObjectException("Invalid key count " + keyCount);
      }

      ImmutableMap.Builder<Object, ImmutableSet<Object>> builder = ImmutableMap.builder();
      int tmpSize = 0;

      for (int i = 0; i < keyCount; i++) {
         Object key = Objects.requireNonNull(stream.readObject());
         int valueCount = stream.readInt();
         if (valueCount <= 0) {
            throw new InvalidObjectException("Invalid value count " + valueCount);
         }

         ImmutableSet.Builder<Object> valuesBuilder = valuesBuilder(valueComparator);

         for (int j = 0; j < valueCount; j++) {
            valuesBuilder.add(Objects.requireNonNull(stream.readObject()));
         }

         ImmutableSet<Object> valueSet = valuesBuilder.build();
         if (valueSet.size() != valueCount) {
            throw new InvalidObjectException("Duplicate key-value pairs exist for key " + key);
         }

         builder.put(key, valueSet);
         tmpSize += valueCount;
      }

      ImmutableMap<Object, ImmutableSet<Object>> tmpMap;
      try {
         tmpMap = builder.buildOrThrow();
      } catch (IllegalArgumentException e) {
         throw (InvalidObjectException)new InvalidObjectException(e.getMessage()).initCause(e);
      }

      ImmutableMultimap.FieldSettersHolder.MAP_FIELD_SETTER.set(this, tmpMap);
      ImmutableMultimap.FieldSettersHolder.SIZE_FIELD_SETTER.set(this, tmpSize);
      ImmutableSetMultimap.SetFieldSettersHolder.EMPTY_SET_FIELD_SETTER.set(this, emptySet(valueComparator));
   }

   public static final class Builder<K, V> extends ImmutableMultimap.Builder<K, V> {
      public Builder() {
      }

      Builder(int expectedKeys) {
         super(expectedKeys);
      }

      @Override
      ImmutableCollection.Builder<V> newValueCollectionBuilderWithExpectedSize(int expectedSize) {
         return this.valueComparator == null
            ? ImmutableSet.builderWithExpectedSize(expectedSize)
            : new ImmutableSortedSet.Builder<>(this.valueComparator, expectedSize);
      }

      @Override
      int expectedValueCollectionSize(int defaultExpectedValues, Iterable<?> values) {
         if (values instanceof Set) {
            Set<?> collection = (Set<?>)values;
            return Math.max(defaultExpectedValues, collection.size());
         } else {
            return defaultExpectedValues;
         }
      }

      @CanIgnoreReturnValue
      public ImmutableSetMultimap.Builder<K, V> expectedValuesPerKey(int expectedValuesPerKey) {
         super.expectedValuesPerKey(expectedValuesPerKey);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSetMultimap.Builder<K, V> put(K key, V value) {
         super.put(key, value);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSetMultimap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         super.put(entry);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSetMultimap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         super.putAll(entries);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSetMultimap.Builder<K, V> putAll(K key, Iterable<? extends V> values) {
         super.putAll(key, values);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSetMultimap.Builder<K, V> putAll(K key, V... values) {
         return this.putAll(key, Arrays.asList(values));
      }

      @CanIgnoreReturnValue
      public ImmutableSetMultimap.Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
         for (Entry<? extends K, ? extends Collection<? extends V>> entry : multimap.asMap().entrySet()) {
            this.putAll((K)entry.getKey(), (Iterable<? extends V>)entry.getValue());
         }

         return this;
      }

      @CanIgnoreReturnValue
      ImmutableSetMultimap.Builder<K, V> combine(ImmutableMultimap.Builder<K, V> other) {
         super.combine(other);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSetMultimap.Builder<K, V> orderKeysBy(Comparator<? super K> keyComparator) {
         super.orderKeysBy(keyComparator);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSetMultimap.Builder<K, V> orderValuesBy(Comparator<? super V> valueComparator) {
         super.orderValuesBy(valueComparator);
         return this;
      }

      public ImmutableSetMultimap<K, V> build() {
         if (this.builderMap == null) {
            return ImmutableSetMultimap.of();
         }

         Collection<Entry<K, ImmutableCollection.Builder<V>>> mapEntries = this.builderMap.entrySet();
         if (this.keyComparator != null) {
            mapEntries = Ordering.from(this.keyComparator).onKeys().immutableSortedCopy(mapEntries);
         }

         return ImmutableSetMultimap.fromMapBuilderEntries(mapEntries, this.valueComparator);
      }
   }

   private static final class EntrySet<K, V> extends ImmutableSet<Entry<K, V>> {
      @Weak
      private final transient ImmutableSetMultimap<K, V> multimap;

      EntrySet(ImmutableSetMultimap<K, V> multimap) {
         this.multimap = multimap;
      }

      @Override
      public boolean contains(@Nullable Object object) {
         if (object instanceof Entry) {
            Entry<?, ?> entry = (Entry<?, ?>)object;
            return this.multimap.containsEntry(entry.getKey(), entry.getValue());
         } else {
            return false;
         }
      }

      @Override
      public int size() {
         return this.multimap.size();
      }

      @Override
      public UnmodifiableIterator<Entry<K, V>> iterator() {
         return this.multimap.entryIterator();
      }

      @Override
      boolean isPartialView() {
         return false;
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }

   @GwtIncompatible
   @J2ktIncompatible
   private static final class SetFieldSettersHolder {
      static final Serialization.FieldSetter<? super ImmutableSetMultimap<?, ?>> EMPTY_SET_FIELD_SETTER = Serialization.getFieldSetter(
         ImmutableSetMultimap.class, "emptySet"
      );
   }
}
