package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
abstract class AbstractNavigableMap<K, V> extends Maps.IteratorBasedAbstractMap<K, V> implements NavigableMap<K, V> {
   @Override
   public abstract @Nullable V get(@Nullable Object key);

   @Override
   public Entry<K, V> firstEntry() {
      return Iterators.getNext(this.entryIterator(), null);
   }

   @Override
   public Entry<K, V> lastEntry() {
      return Iterators.getNext(this.descendingEntryIterator(), null);
   }

   @Override
   public @Nullable Entry<K, V> pollFirstEntry() {
      return Iterators.pollNext(this.entryIterator());
   }

   @Override
   public @Nullable Entry<K, V> pollLastEntry() {
      return Iterators.pollNext(this.descendingEntryIterator());
   }

   @ParametricNullness
   @Override
   public K firstKey() {
      Entry<K, V> entry = this.firstEntry();
      if (entry == null) {
         throw new NoSuchElementException();
      } else {
         return entry.getKey();
      }
   }

   @ParametricNullness
   @Override
   public K lastKey() {
      Entry<K, V> entry = this.lastEntry();
      if (entry == null) {
         throw new NoSuchElementException();
      } else {
         return entry.getKey();
      }
   }

   @Override
   public @Nullable Entry<K, V> lowerEntry(@ParametricNullness K key) {
      return this.headMap(key, false).lastEntry();
   }

   @Override
   public @Nullable Entry<K, V> floorEntry(@ParametricNullness K key) {
      return this.headMap(key, true).lastEntry();
   }

   @Override
   public @Nullable Entry<K, V> ceilingEntry(@ParametricNullness K key) {
      return this.tailMap(key, true).firstEntry();
   }

   @Override
   public @Nullable Entry<K, V> higherEntry(@ParametricNullness K key) {
      return this.tailMap(key, false).firstEntry();
   }

   @Override
   public @Nullable K lowerKey(@ParametricNullness K key) {
      return Maps.keyOrNull(this.lowerEntry(key));
   }

   @Override
   public @Nullable K floorKey(@ParametricNullness K key) {
      return Maps.keyOrNull(this.floorEntry(key));
   }

   @Override
   public @Nullable K ceilingKey(@ParametricNullness K key) {
      return Maps.keyOrNull(this.ceilingEntry(key));
   }

   @Override
   public @Nullable K higherKey(@ParametricNullness K key) {
      return Maps.keyOrNull(this.higherEntry(key));
   }

   abstract Iterator<Entry<K, V>> descendingEntryIterator();

   @Override
   public SortedMap<K, V> subMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
      return this.subMap(fromKey, true, toKey, false);
   }

   @Override
   public SortedMap<K, V> headMap(@ParametricNullness K toKey) {
      return this.headMap(toKey, false);
   }

   @Override
   public SortedMap<K, V> tailMap(@ParametricNullness K fromKey) {
      return this.tailMap(fromKey, true);
   }

   @Override
   public NavigableSet<K> navigableKeySet() {
      return new Maps.NavigableKeySet<>(this);
   }

   @Override
   public Set<K> keySet() {
      return this.navigableKeySet();
   }

   @Override
   public NavigableSet<K> descendingKeySet() {
      return this.descendingMap().navigableKeySet();
   }

   @Override
   public NavigableMap<K, V> descendingMap() {
      return new AbstractNavigableMap.DescendingMap();
   }

   private final class DescendingMap extends Maps.DescendingMap<K, V> {
      private DescendingMap() {
      }

      @Override
      NavigableMap<K, V> forward() {
         return AbstractNavigableMap.this;
      }

      @Override
      Iterator<Entry<K, V>> entryIterator() {
         return AbstractNavigableMap.this.descendingEntryIterator();
      }
   }
}
