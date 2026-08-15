package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractMultimap<K, V> implements Multimap<K, V> {
   @LazyInit
   private transient @Nullable Collection<Entry<K, V>> entries;
   @LazyInit
   private transient @Nullable Set<K> keySet;
   @LazyInit
   private transient @Nullable Multiset<K> keys;
   @LazyInit
   private transient @Nullable Collection<V> values;
   @LazyInit
   private transient @Nullable Map<K, Collection<V>> asMap;

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      for (Collection<V> collection : this.asMap().values()) {
         if (collection.contains(value)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
      Collection<V> collection = this.asMap().get(key);
      return collection != null && collection.contains(value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean remove(@Nullable Object key, @Nullable Object value) {
      Collection<V> collection = this.asMap().get(key);
      return collection != null && collection.remove(value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean put(@ParametricNullness K key, @ParametricNullness V value) {
      return this.get(key).add(value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean putAll(@ParametricNullness K key, Iterable<? extends V> values) {
      Preconditions.checkNotNull(values);
      if (values instanceof Collection) {
         Collection<? extends V> valueCollection = (Collection<? extends V>)values;
         return !valueCollection.isEmpty() && this.get(key).addAll(valueCollection);
      } else {
         Iterator<? extends V> valueItr = values.iterator();
         return valueItr.hasNext() && Iterators.addAll(this.get(key), valueItr);
      }
   }

   @CanIgnoreReturnValue
   @Override
   public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
      boolean changed = false;

      for (Entry<? extends K, ? extends V> entry : multimap.entries()) {
         changed |= this.put((K)entry.getKey(), (V)entry.getValue());
      }

      return changed;
   }

   @CanIgnoreReturnValue
   @Override
   public Collection<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      Preconditions.checkNotNull(values);
      Collection<V> result = this.removeAll(key);
      this.putAll(key, values);
      return result;
   }

   @Override
   public Collection<Entry<K, V>> entries() {
      Collection<Entry<K, V>> result = this.entries;
      return result == null ? (this.entries = this.createEntries()) : result;
   }

   abstract Collection<Entry<K, V>> createEntries();

   abstract Iterator<Entry<K, V>> entryIterator();

   Spliterator<Entry<K, V>> entrySpliterator() {
      return Spliterators.spliterator(this.entryIterator(), this.size(), this instanceof SetMultimap ? 1 : 0);
   }

   @Override
   public Set<K> keySet() {
      Set<K> result = this.keySet;
      return result == null ? (this.keySet = this.createKeySet()) : result;
   }

   abstract Set<K> createKeySet();

   @Override
   public Multiset<K> keys() {
      Multiset<K> result = this.keys;
      return result == null ? (this.keys = this.createKeys()) : result;
   }

   abstract Multiset<K> createKeys();

   @Override
   public Collection<V> values() {
      Collection<V> result = this.values;
      return result == null ? (this.values = this.createValues()) : result;
   }

   abstract Collection<V> createValues();

   Iterator<V> valueIterator() {
      return Maps.valueIterator(this.entries().iterator());
   }

   Spliterator<V> valueSpliterator() {
      return Spliterators.spliterator(this.valueIterator(), this.size(), 0);
   }

   @Override
   public Map<K, Collection<V>> asMap() {
      Map<K, Collection<V>> result = this.asMap;
      return result == null ? (this.asMap = this.createAsMap()) : result;
   }

   abstract Map<K, Collection<V>> createAsMap();

   @Override
   public boolean equals(@Nullable Object object) {
      return Multimaps.equalsImpl(this, object);
   }

   @Override
   public int hashCode() {
      return this.asMap().hashCode();
   }

   @Override
   public String toString() {
      return this.asMap().toString();
   }

   class Entries extends Multimaps.Entries<K, V> {
      @Override
      Multimap<K, V> multimap() {
         return AbstractMultimap.this;
      }

      @Override
      public Iterator<Entry<K, V>> iterator() {
         return AbstractMultimap.this.entryIterator();
      }

      @Override
      public Spliterator<Entry<K, V>> spliterator() {
         return AbstractMultimap.this.entrySpliterator();
      }
   }

   class EntrySet extends AbstractMultimap<K, V>.Entries implements Set<Entry<K, V>> {
      @Override
      public int hashCode() {
         return Sets.hashCodeImpl(this);
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         return Sets.equalsImpl(this, obj);
      }
   }

   class Values extends AbstractCollection<V> {
      @Override
      public Iterator<V> iterator() {
         return AbstractMultimap.this.valueIterator();
      }

      @Override
      public Spliterator<V> spliterator() {
         return AbstractMultimap.this.valueSpliterator();
      }

      @Override
      public int size() {
         return AbstractMultimap.this.size();
      }

      @Override
      public boolean contains(@Nullable Object o) {
         return AbstractMultimap.this.containsValue(o);
      }

      @Override
      public void clear() {
         AbstractMultimap.this.clear();
      }
   }
}
