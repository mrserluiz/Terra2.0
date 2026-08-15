package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractListMultimap<K, V> extends AbstractMapBasedMultimap<K, V> implements ListMultimap<K, V> {
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 6588350623831699109L;

   protected AbstractListMultimap(Map<K, Collection<V>> map) {
      super(map);
   }

   abstract List<V> createCollection();

   List<V> createUnmodifiableEmptyCollection() {
      return Collections.emptyList();
   }

   @Override
   <E> Collection<E> unmodifiableCollectionSubclass(Collection<E> collection) {
      return Collections.unmodifiableList((List<? extends E>)collection);
   }

   @Override
   Collection<V> wrapCollection(@ParametricNullness K key, Collection<V> collection) {
      return this.wrapList(key, (List<V>)collection, null);
   }

   @Override
   public List<V> get(@ParametricNullness K key) {
      return (List<V>)super.get(key);
   }

   @CanIgnoreReturnValue
   @Override
   public List<V> removeAll(@Nullable Object key) {
      return (List<V>)super.removeAll(key);
   }

   @CanIgnoreReturnValue
   @Override
   public List<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      return (List<V>)super.replaceValues(key, values);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean put(@ParametricNullness K key, @ParametricNullness V value) {
      return super.put(key, value);
   }

   @Override
   public Map<K, Collection<V>> asMap() {
      return super.asMap();
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return super.equals(object);
   }
}
