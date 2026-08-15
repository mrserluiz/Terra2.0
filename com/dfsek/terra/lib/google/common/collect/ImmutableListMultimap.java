package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
public class ImmutableListMultimap<K, V> extends ImmutableMultimap<K, V> implements ListMultimap<K, V> {
   @LazyInit
   @RetainedWith
   private transient @Nullable ImmutableListMultimap<V, K> inverse;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> toImmutableListMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      return CollectCollectors.toImmutableListMultimap(keyFunction, valueFunction);
   }

   public static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> flatteningToImmutableListMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends Stream<? extends V>> valuesFunction
   ) {
      return CollectCollectors.flatteningToImmutableListMultimap(keyFunction, valuesFunction);
   }

   public static <K, V> ImmutableListMultimap<K, V> of() {
      return EmptyImmutableListMultimap.INSTANCE;
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      builder.put(k3, v3);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      builder.put(k3, v3);
      builder.put(k4, v4);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      builder.put(k3, v3);
      builder.put(k4, v4);
      builder.put(k5, v5);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap.Builder<K, V> builder() {
      return new ImmutableListMultimap.Builder<>();
   }

   public static <K, V> ImmutableListMultimap.Builder<K, V> builderWithExpectedKeys(int expectedKeys) {
      CollectPreconditions.checkNonnegative(expectedKeys, "expectedKeys");
      return new ImmutableListMultimap.Builder<>(expectedKeys);
   }

   public static <K, V> ImmutableListMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap) {
      if (multimap.isEmpty()) {
         return of();
      }

      if (multimap instanceof ImmutableListMultimap) {
         ImmutableListMultimap<K, V> kvMultimap = (ImmutableListMultimap<K, V>)multimap;
         if (!kvMultimap.isPartialView()) {
            return kvMultimap;
         }
      }

      return fromMapEntries(multimap.asMap().entrySet(), null);
   }

   public static <K, V> ImmutableListMultimap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      return new ImmutableListMultimap.Builder<K, V>().putAll(entries).build();
   }

   static <K, V> ImmutableListMultimap<K, V> fromMapEntries(
      Collection<? extends Entry<? extends K, ? extends Collection<? extends V>>> mapEntries, @Nullable Comparator<? super V> valueComparator
   ) {
      if (mapEntries.isEmpty()) {
         return of();
      }

      ImmutableMap.Builder<K, ImmutableList<V>> builder = new ImmutableMap.Builder<>(mapEntries.size());
      int size = 0;

      for (Entry<? extends K, ? extends Collection<? extends V>> entry : mapEntries) {
         K key = (K)entry.getKey();
         Collection<? extends V> values = (Collection<? extends V>)entry.getValue();
         ImmutableList<V> list = valueComparator == null ? ImmutableList.copyOf(values) : ImmutableList.sortedCopyOf(valueComparator, values);
         if (!list.isEmpty()) {
            builder.put(key, list);
            size += list.size();
         }
      }

      return new ImmutableListMultimap<>(builder.buildOrThrow(), size);
   }

   static <K, V> ImmutableListMultimap<K, V> fromMapBuilderEntries(
      Collection<? extends Entry<K, ImmutableCollection.Builder<V>>> mapEntries, @Nullable Comparator<? super V> valueComparator
   ) {
      if (mapEntries.isEmpty()) {
         return of();
      }

      ImmutableMap.Builder<K, ImmutableList<V>> builder = new ImmutableMap.Builder<>(mapEntries.size());
      int size = 0;

      for (Entry<K, ImmutableCollection.Builder<V>> entry : mapEntries) {
         K key = entry.getKey();
         ImmutableList.Builder<V> values = (ImmutableList.Builder<V>)entry.getValue();
         ImmutableList<V> list = valueComparator == null ? values.build() : values.buildSorted(valueComparator);
         builder.put(key, list);
         size += list.size();
      }

      return new ImmutableListMultimap<>(builder.buildOrThrow(), size);
   }

   ImmutableListMultimap(ImmutableMap<K, ImmutableList<V>> map, int size) {
      super(map, size);
   }

   public ImmutableList<V> get(K key) {
      ImmutableList<V> list = (ImmutableList<V>)this.map.get(key);
      return list == null ? ImmutableList.of() : list;
   }

   public ImmutableListMultimap<V, K> inverse() {
      ImmutableListMultimap<V, K> result = this.inverse;
      return result == null ? (this.inverse = this.invert()) : result;
   }

   private ImmutableListMultimap<V, K> invert() {
      ImmutableListMultimap.Builder<V, K> builder = builder();

      for (Entry<K, V> entry : this.entries()) {
         builder.put(entry.getValue(), entry.getKey());
      }

      ImmutableListMultimap<V, K> invertedMultimap = builder.build();
      invertedMultimap.inverse = this;
      return invertedMultimap;
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   public final ImmutableList<V> removeAll(@Nullable Object key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   public final ImmutableList<V> replaceValues(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      Serialization.writeMultimap(this, stream);
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      int keyCount = stream.readInt();
      if (keyCount < 0) {
         throw new InvalidObjectException("Invalid key count " + keyCount);
      }

      ImmutableMap.Builder<Object, ImmutableList<Object>> builder = ImmutableMap.builder();
      int tmpSize = 0;

      for (int i = 0; i < keyCount; i++) {
         Object key = Objects.requireNonNull(stream.readObject());
         int valueCount = stream.readInt();
         if (valueCount <= 0) {
            throw new InvalidObjectException("Invalid value count " + valueCount);
         }

         ImmutableList.Builder<Object> valuesBuilder = ImmutableList.builder();

         for (int j = 0; j < valueCount; j++) {
            valuesBuilder.add(Objects.requireNonNull(stream.readObject()));
         }

         builder.put(key, valuesBuilder.build());
         tmpSize += valueCount;
      }

      ImmutableMap<Object, ImmutableList<Object>> tmpMap;
      try {
         tmpMap = builder.buildOrThrow();
      } catch (IllegalArgumentException e) {
         throw (InvalidObjectException)new InvalidObjectException(e.getMessage()).initCause(e);
      }

      ImmutableMultimap.FieldSettersHolder.MAP_FIELD_SETTER.set(this, tmpMap);
      ImmutableMultimap.FieldSettersHolder.SIZE_FIELD_SETTER.set(this, tmpSize);
   }

   public static final class Builder<K, V> extends ImmutableMultimap.Builder<K, V> {
      public Builder() {
      }

      Builder(int expectedKeys) {
         super(expectedKeys);
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> expectedValuesPerKey(int expectedValuesPerKey) {
         super.expectedValuesPerKey(expectedValuesPerKey);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> put(K key, V value) {
         super.put(key, value);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         super.put(entry);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         super.putAll(entries);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> putAll(K key, Iterable<? extends V> values) {
         super.putAll(key, values);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> putAll(K key, V... values) {
         super.putAll(key, values);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
         super.putAll(multimap);
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableListMultimap.Builder<K, V> combine(ImmutableMultimap.Builder<K, V> other) {
         super.combine(other);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> orderKeysBy(Comparator<? super K> keyComparator) {
         super.orderKeysBy(keyComparator);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> orderValuesBy(Comparator<? super V> valueComparator) {
         super.orderValuesBy(valueComparator);
         return this;
      }

      public ImmutableListMultimap<K, V> build() {
         return (ImmutableListMultimap<K, V>)super.build();
      }
   }
}
