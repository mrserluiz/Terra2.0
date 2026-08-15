package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
public abstract class ForwardingNavigableMap<K, V> extends ForwardingSortedMap<K, V> implements NavigableMap<K, V> {
   protected ForwardingNavigableMap() {
   }

   protected abstract NavigableMap<K, V> delegate();

   @Override
   public @Nullable Entry<K, V> lowerEntry(@ParametricNullness K key) {
      return this.delegate().lowerEntry(key);
   }

   protected @Nullable Entry<K, V> standardLowerEntry(@ParametricNullness K key) {
      return this.headMap(key, false).lastEntry();
   }

   @Override
   public @Nullable K lowerKey(@ParametricNullness K key) {
      return this.delegate().lowerKey(key);
   }

   protected @Nullable K standardLowerKey(@ParametricNullness K key) {
      return Maps.keyOrNull(this.lowerEntry(key));
   }

   @Override
   public @Nullable Entry<K, V> floorEntry(@ParametricNullness K key) {
      return this.delegate().floorEntry(key);
   }

   protected @Nullable Entry<K, V> standardFloorEntry(@ParametricNullness K key) {
      return this.headMap(key, true).lastEntry();
   }

   @Override
   public @Nullable K floorKey(@ParametricNullness K key) {
      return this.delegate().floorKey(key);
   }

   protected @Nullable K standardFloorKey(@ParametricNullness K key) {
      return Maps.keyOrNull(this.floorEntry(key));
   }

   @Override
   public @Nullable Entry<K, V> ceilingEntry(@ParametricNullness K key) {
      return this.delegate().ceilingEntry(key);
   }

   protected @Nullable Entry<K, V> standardCeilingEntry(@ParametricNullness K key) {
      return this.tailMap(key, true).firstEntry();
   }

   @Override
   public @Nullable K ceilingKey(@ParametricNullness K key) {
      return this.delegate().ceilingKey(key);
   }

   protected @Nullable K standardCeilingKey(@ParametricNullness K key) {
      return Maps.keyOrNull(this.ceilingEntry(key));
   }

   @Override
   public @Nullable Entry<K, V> higherEntry(@ParametricNullness K key) {
      return this.delegate().higherEntry(key);
   }

   protected @Nullable Entry<K, V> standardHigherEntry(@ParametricNullness K key) {
      return this.tailMap(key, false).firstEntry();
   }

   @Override
   public @Nullable K higherKey(@ParametricNullness K key) {
      return this.delegate().higherKey(key);
   }

   protected @Nullable K standardHigherKey(@ParametricNullness K key) {
      return Maps.keyOrNull(this.higherEntry(key));
   }

   @Override
   public @Nullable Entry<K, V> firstEntry() {
      return this.delegate().firstEntry();
   }

   protected Entry<K, V> standardFirstEntry() {
      return Iterables.getFirst(this.entrySet(), null);
   }

   protected K standardFirstKey() {
      Entry<K, V> entry = this.firstEntry();
      if (entry == null) {
         throw new NoSuchElementException();
      } else {
         return entry.getKey();
      }
   }

   @Override
   public @Nullable Entry<K, V> lastEntry() {
      return this.delegate().lastEntry();
   }

   protected Entry<K, V> standardLastEntry() {
      return Iterables.getFirst(this.descendingMap().entrySet(), null);
   }

   protected K standardLastKey() {
      Entry<K, V> entry = this.lastEntry();
      if (entry == null) {
         throw new NoSuchElementException();
      } else {
         return entry.getKey();
      }
   }

   @Override
   public @Nullable Entry<K, V> pollFirstEntry() {
      return this.delegate().pollFirstEntry();
   }

   protected @Nullable Entry<K, V> standardPollFirstEntry() {
      return Iterators.pollNext(this.entrySet().iterator());
   }

   @Override
   public @Nullable Entry<K, V> pollLastEntry() {
      return this.delegate().pollLastEntry();
   }

   protected @Nullable Entry<K, V> standardPollLastEntry() {
      return Iterators.pollNext(this.descendingMap().entrySet().iterator());
   }

   @Override
   public NavigableMap<K, V> descendingMap() {
      return this.delegate().descendingMap();
   }

   @Override
   public NavigableSet<K> navigableKeySet() {
      return this.delegate().navigableKeySet();
   }

   @Override
   public NavigableSet<K> descendingKeySet() {
      return this.delegate().descendingKeySet();
   }

   protected NavigableSet<K> standardDescendingKeySet() {
      return this.descendingMap().navigableKeySet();
   }

   @Override
   protected SortedMap<K, V> standardSubMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
      return this.subMap(fromKey, true, toKey, false);
   }

   @Override
   public NavigableMap<K, V> subMap(@ParametricNullness K fromKey, boolean fromInclusive, @ParametricNullness K toKey, boolean toInclusive) {
      return this.delegate().subMap(fromKey, fromInclusive, toKey, toInclusive);
   }

   @Override
   public NavigableMap<K, V> headMap(@ParametricNullness K toKey, boolean inclusive) {
      return this.delegate().headMap(toKey, inclusive);
   }

   @Override
   public NavigableMap<K, V> tailMap(@ParametricNullness K fromKey, boolean inclusive) {
      return this.delegate().tailMap(fromKey, inclusive);
   }

   protected SortedMap<K, V> standardHeadMap(@ParametricNullness K toKey) {
      return this.headMap(toKey, false);
   }

   protected SortedMap<K, V> standardTailMap(@ParametricNullness K fromKey) {
      return this.tailMap(fromKey, true);
   }

   protected class StandardDescendingMap extends Maps.DescendingMap<K, V> {
      public StandardDescendingMap() {
      }

      @Override
      NavigableMap<K, V> forward() {
         return ForwardingNavigableMap.this;
      }

      @Override
      public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
         this.forward().replaceAll(function);
      }

      @Override
      protected Iterator<Entry<K, V>> entryIterator() {
         return new Iterator<Entry<K, V>>() {
            private @Nullable Entry<K, V> toRemove = null;
            private @Nullable Entry<K, V> nextOrNull = StandardDescendingMap.this.forward().lastEntry();

            @Override
            public boolean hasNext() {
               return this.nextOrNull != null;
            }

            public Entry<K, V> next() {
               if (this.nextOrNull == null) {
                  throw new NoSuchElementException();
               }

               try {
                  return this.nextOrNull;
               } finally {
                  this.toRemove = this.nextOrNull;
                  this.nextOrNull = StandardDescendingMap.this.forward().lowerEntry(this.nextOrNull.getKey());
               }
            }

            @Override
            public void remove() {
               if (this.toRemove == null) {
                  throw new IllegalStateException("no calls to next() since the last call to remove()");
               }

               StandardDescendingMap.this.forward().remove(this.toRemove.getKey());
               this.toRemove = null;
            }
         };
      }
   }

   protected class StandardNavigableKeySet extends Maps.NavigableKeySet<K, V> {
      public StandardNavigableKeySet() {
         super(ForwardingNavigableMap.this);
      }
   }
}
