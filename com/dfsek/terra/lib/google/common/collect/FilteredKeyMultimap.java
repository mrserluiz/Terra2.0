package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
class FilteredKeyMultimap<K, V> extends AbstractMultimap<K, V> implements FilteredMultimap<K, V> {
   final Multimap<K, V> unfiltered;
   final Predicate<? super K> keyPredicate;

   FilteredKeyMultimap(Multimap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
      this.unfiltered = Preconditions.checkNotNull(unfiltered);
      this.keyPredicate = Preconditions.checkNotNull(keyPredicate);
   }

   @Override
   public Multimap<K, V> unfiltered() {
      return this.unfiltered;
   }

   @Override
   public Predicate<? super Entry<K, V>> entryPredicate() {
      return Maps.keyPredicateOnEntries(this.keyPredicate);
   }

   @Override
   public int size() {
      int size = 0;

      for (Collection<V> collection : this.asMap().values()) {
         size += collection.size();
      }

      return size;
   }

   @Override
   public boolean containsKey(@Nullable Object key) {
      if (this.unfiltered.containsKey(key)) {
         K k = (K)key;
         return this.keyPredicate.apply(k);
      } else {
         return false;
      }
   }

   @Override
   public Collection<V> removeAll(@Nullable Object key) {
      return this.containsKey(key) ? this.unfiltered.removeAll(key) : this.unmodifiableEmptyCollection();
   }

   Collection<V> unmodifiableEmptyCollection() {
      return this.unfiltered instanceof SetMultimap ? Collections.emptySet() : Collections.emptyList();
   }

   @Override
   public void clear() {
      this.keySet().clear();
   }

   @Override
   Set<K> createKeySet() {
      return Sets.filter(this.unfiltered.keySet(), this.keyPredicate);
   }

   @Override
   public Collection<V> get(@ParametricNullness K key) {
      if (this.keyPredicate.apply(key)) {
         return this.unfiltered.get(key);
      } else {
         return this.unfiltered instanceof SetMultimap ? new FilteredKeyMultimap.AddRejectingSet<>(key) : new FilteredKeyMultimap.AddRejectingList<>(key);
      }
   }

   @Override
   Iterator<Entry<K, V>> entryIterator() {
      throw new AssertionError("should never be called");
   }

   @Override
   Collection<Entry<K, V>> createEntries() {
      return new FilteredKeyMultimap.Entries();
   }

   @Override
   Collection<V> createValues() {
      return new FilteredMultimapValues<>(this);
   }

   @Override
   Map<K, Collection<V>> createAsMap() {
      return Maps.filterKeys(this.unfiltered.asMap(), this.keyPredicate);
   }

   @Override
   Multiset<K> createKeys() {
      return Multisets.filter(this.unfiltered.keys(), this.keyPredicate);
   }

   static class AddRejectingList<K, V> extends ForwardingList<V> {
      @ParametricNullness
      final K key;

      AddRejectingList(@ParametricNullness K key) {
         this.key = key;
      }

      @Override
      public boolean add(@ParametricNullness V v) {
         this.add(0, v);
         return true;
      }

      @Override
      public void add(int index, @ParametricNullness V element) {
         Preconditions.checkPositionIndex(index, 0);
         throw new IllegalArgumentException("Key does not satisfy predicate: " + this.key);
      }

      @Override
      public boolean addAll(Collection<? extends V> collection) {
         this.addAll(0, collection);
         return true;
      }

      @CanIgnoreReturnValue
      @Override
      public boolean addAll(int index, Collection<? extends V> elements) {
         Preconditions.checkNotNull(elements);
         Preconditions.checkPositionIndex(index, 0);
         throw new IllegalArgumentException("Key does not satisfy predicate: " + this.key);
      }

      @Override
      protected List<V> delegate() {
         return Collections.emptyList();
      }
   }

   static class AddRejectingSet<K, V> extends ForwardingSet<V> {
      @ParametricNullness
      final K key;

      AddRejectingSet(@ParametricNullness K key) {
         this.key = key;
      }

      @Override
      public boolean add(@ParametricNullness V element) {
         throw new IllegalArgumentException("Key does not satisfy predicate: " + this.key);
      }

      @Override
      public boolean addAll(Collection<? extends V> collection) {
         Preconditions.checkNotNull(collection);
         throw new IllegalArgumentException("Key does not satisfy predicate: " + this.key);
      }

      @Override
      protected Set<V> delegate() {
         return Collections.emptySet();
      }
   }

   class Entries extends ForwardingCollection<Entry<K, V>> {
      @Override
      protected Collection<Entry<K, V>> delegate() {
         return Collections2.filter(FilteredKeyMultimap.this.unfiltered.entries(), FilteredKeyMultimap.this.entryPredicate());
      }

      @Override
      public boolean remove(@Nullable Object o) {
         if (o instanceof Entry) {
            Entry<?, ?> entry = (Entry<?, ?>)o;
            if (FilteredKeyMultimap.this.unfiltered.containsKey(entry.getKey()) && FilteredKeyMultimap.this.keyPredicate.apply((K)entry.getKey())) {
               return FilteredKeyMultimap.this.unfiltered.remove(entry.getKey(), entry.getValue());
            }
         }

         return false;
      }
   }
}
