package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public interface SortedSetMultimap<K, V> extends SetMultimap<K, V> {
   SortedSet<V> get(@ParametricNullness K key);

   @CanIgnoreReturnValue
   SortedSet<V> removeAll(@Nullable Object key);

   @CanIgnoreReturnValue
   SortedSet<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values);

   @Override
   Map<K, Collection<V>> asMap();

   @Nullable Comparator<? super V> valueComparator();
}
