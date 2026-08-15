package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractSortedSetMultimap<K, V> extends AbstractSetMultimap<K, V> implements SortedSetMultimap<K, V> {
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 430848587173315748L;

   protected AbstractSortedSetMultimap(Map<K, Collection<V>> map) {
      super(map);
   }

   abstract SortedSet<V> createCollection();

   SortedSet<V> createUnmodifiableEmptyCollection() {
      return this.unmodifiableCollectionSubclass(this.createCollection());
   }

   <E> SortedSet<E> unmodifiableCollectionSubclass(Collection<E> collection) {
      return collection instanceof NavigableSet
         ? Sets.unmodifiableNavigableSet((NavigableSet<E>)collection)
         : Collections.unmodifiableSortedSet((SortedSet<E>)collection);
   }

   @Override
   Collection<V> wrapCollection(@ParametricNullness K key, Collection<V> collection) {
      return collection instanceof NavigableSet
         ? new AbstractMapBasedMultimap.WrappedNavigableSet(key, (NavigableSet<V>)collection, null)
         : new AbstractMapBasedMultimap.WrappedSortedSet(key, (SortedSet<V>)collection, null);
   }

   @Override
   public SortedSet<V> get(@ParametricNullness K key) {
      return (SortedSet<V>)super.get(key);
   }

   @CanIgnoreReturnValue
   @Override
   public SortedSet<V> removeAll(@Nullable Object key) {
      return (SortedSet<V>)super.removeAll(key);
   }

   @CanIgnoreReturnValue
   @Override
   public SortedSet<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      return (SortedSet<V>)super.replaceValues(key, values);
   }

   @Override
   public Map<K, Collection<V>> asMap() {
      return super.asMap();
   }

   @Override
   public Collection<V> values() {
      return super.values();
   }
}
