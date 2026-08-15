package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use ImmutableRangeMap or TreeRangeMap")
@GwtIncompatible
public interface RangeMap<K extends Comparable, V> {
   @Nullable V get(K key);

   @Nullable Entry<Range<K>, V> getEntry(K key);

   Range<K> span();

   void put(Range<K> range, V value);

   void putCoalescing(Range<K> range, V value);

   void putAll(RangeMap<K, ? extends V> rangeMap);

   void clear();

   void remove(Range<K> range);

   void merge(Range<K> range, @Nullable V value, BiFunction<? super V, ? super @Nullable V, ? extends @Nullable V> remappingFunction);

   Map<Range<K>, V> asMapOfRanges();

   Map<Range<K>, V> asDescendingMapOfRanges();

   RangeMap<K, V> subRangeMap(Range<K> range);

   @Override
   boolean equals(@Nullable Object o);

   @Override
   int hashCode();

   @Override
   String toString();
}
