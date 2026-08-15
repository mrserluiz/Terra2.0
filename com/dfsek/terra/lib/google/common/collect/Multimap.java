package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CompatibleWith;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use ImmutableMultimap, HashMultimap, or another implementation")
@GwtCompatible
public interface Multimap<K, V> {
   int size();

   boolean isEmpty();

   boolean containsKey(@CompatibleWith("K") @Nullable Object key);

   boolean containsValue(@CompatibleWith("V") @Nullable Object value);

   boolean containsEntry(@CompatibleWith("K") @Nullable Object key, @CompatibleWith("V") @Nullable Object value);

   @CanIgnoreReturnValue
   boolean put(@ParametricNullness K key, @ParametricNullness V value);

   @CanIgnoreReturnValue
   boolean remove(@CompatibleWith("K") @Nullable Object key, @CompatibleWith("V") @Nullable Object value);

   @CanIgnoreReturnValue
   boolean putAll(@ParametricNullness K key, Iterable<? extends V> values);

   @CanIgnoreReturnValue
   boolean putAll(Multimap<? extends K, ? extends V> multimap);

   @CanIgnoreReturnValue
   Collection<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values);

   @CanIgnoreReturnValue
   Collection<V> removeAll(@CompatibleWith("K") @Nullable Object key);

   void clear();

   Collection<V> get(@ParametricNullness K key);

   Set<K> keySet();

   Multiset<K> keys();

   Collection<V> values();

   Collection<Entry<K, V>> entries();

   default void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);
      this.entries().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
   }

   Map<K, Collection<V>> asMap();

   @Override
   boolean equals(@Nullable Object obj);

   @Override
   int hashCode();
}
