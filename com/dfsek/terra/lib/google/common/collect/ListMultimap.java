package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public interface ListMultimap<K, V> extends Multimap<K, V> {
   List<V> get(@ParametricNullness K key);

   @CanIgnoreReturnValue
   List<V> removeAll(@Nullable Object key);

   @CanIgnoreReturnValue
   List<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values);

   @Override
   Map<K, Collection<V>> asMap();

   @Override
   boolean equals(@Nullable Object obj);
}
