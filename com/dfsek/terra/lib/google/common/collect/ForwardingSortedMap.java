package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingSortedMap<K, V> extends ForwardingMap<K, V> implements SortedMap<K, V> {
   protected ForwardingSortedMap() {
   }

   protected abstract SortedMap<K, V> delegate();

   @Override
   public @Nullable Comparator<? super K> comparator() {
      return this.delegate().comparator();
   }

   @ParametricNullness
   @Override
   public K firstKey() {
      return this.delegate().firstKey();
   }

   @Override
   public SortedMap<K, V> headMap(@ParametricNullness K toKey) {
      return this.delegate().headMap(toKey);
   }

   @ParametricNullness
   @Override
   public K lastKey() {
      return this.delegate().lastKey();
   }

   @Override
   public SortedMap<K, V> subMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
      return this.delegate().subMap(fromKey, toKey);
   }

   @Override
   public SortedMap<K, V> tailMap(@ParametricNullness K fromKey) {
      return this.delegate().tailMap(fromKey);
   }

   static int unsafeCompare(Comparator<?> comparator, Object o1, Object o2) {
      return comparator == null ? ((Comparable)o1).compareTo(o2) : ((Comparator<Object>)comparator).compare(o1, o2);
   }

   @Override
   protected boolean standardContainsKey(Object key) {
      try {
         SortedMap<Object, V> self = this;
         Object ceilingKey = self.tailMap(key).firstKey();
         return unsafeCompare(this.comparator(), ceilingKey, key) == 0;
      } catch (ClassCastException | NoSuchElementException | NullPointerException e) {
         return false;
      }
   }

   protected SortedMap<K, V> standardSubMap(K fromKey, K toKey) {
      Preconditions.checkArgument(unsafeCompare(this.comparator(), fromKey, toKey) <= 0, "fromKey must be <= toKey");
      return this.tailMap(fromKey).headMap(toKey);
   }

   protected class StandardKeySet extends Maps.SortedKeySet<K, V> {
      public StandardKeySet() {
         super(ForwardingSortedMap.this);
      }
   }
}
