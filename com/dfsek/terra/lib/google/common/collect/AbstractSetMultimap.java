package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractSetMultimap<K, V> extends AbstractMapBasedMultimap<K, V> implements SetMultimap<K, V> {
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 7431625294878419160L;

   protected AbstractSetMultimap(Map<K, Collection<V>> map) {
      super(map);
   }

   abstract Set<V> createCollection();

   Set<V> createUnmodifiableEmptyCollection() {
      return Collections.emptySet();
   }

   @Override
   <E> Collection<E> unmodifiableCollectionSubclass(Collection<E> collection) {
      return Collections.unmodifiableSet((Set<? extends E>)collection);
   }

   @Override
   Collection<V> wrapCollection(@ParametricNullness K key, Collection<V> collection) {
      return new AbstractMapBasedMultimap.WrappedSet(key, (Set<V>)collection);
   }

   @Override
   public Set<V> get(@ParametricNullness K key) {
      return (Set<V>)super.get(key);
   }

   @Override
   public Set<Entry<K, V>> entries() {
      return (Set<Entry<K, V>>)super.entries();
   }

   @CanIgnoreReturnValue
   @Override
   public Set<V> removeAll(@Nullable Object key) {
      return (Set<V>)super.removeAll(key);
   }

   @CanIgnoreReturnValue
   @Override
   public Set<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      return (Set<V>)super.replaceValues(key, values);
   }

   @Override
   public Map<K, Collection<V>> asMap() {
      return super.asMap();
   }

   @CanIgnoreReturnValue
   @Override
   public boolean put(@ParametricNullness K key, @ParametricNullness V value) {
      return super.put(key, value);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return super.equals(object);
   }
}
