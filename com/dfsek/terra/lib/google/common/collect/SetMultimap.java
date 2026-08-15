package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public interface SetMultimap<K, V> extends Multimap<K, V> {
   Set<V> get(@ParametricNullness K key);

   @CanIgnoreReturnValue
   Set<V> removeAll(@Nullable Object key);

   @CanIgnoreReturnValue
   Set<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values);

   Set<Entry<K, V>> entries();

   @Override
   Map<K, Collection<V>> asMap();

   @Override
   boolean equals(@Nullable Object obj);
}
