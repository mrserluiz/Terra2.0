package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingMultimap<K, V> extends ForwardingObject implements Multimap<K, V> {
   protected ForwardingMultimap() {
   }

   protected abstract Multimap<K, V> delegate();

   @Override
   public Map<K, Collection<V>> asMap() {
      return this.delegate().asMap();
   }

   @Override
   public void clear() {
      this.delegate().clear();
   }

   @Override
   public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
      return this.delegate().containsEntry(key, value);
   }

   @Override
   public boolean containsKey(@Nullable Object key) {
      return this.delegate().containsKey(key);
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      return this.delegate().containsValue(value);
   }

   @Override
   public Collection<Entry<K, V>> entries() {
      return this.delegate().entries();
   }

   @Override
   public Collection<V> get(@ParametricNullness K key) {
      return this.delegate().get(key);
   }

   @Override
   public boolean isEmpty() {
      return this.delegate().isEmpty();
   }

   @Override
   public Multiset<K> keys() {
      return this.delegate().keys();
   }

   @Override
   public Set<K> keySet() {
      return this.delegate().keySet();
   }

   @CanIgnoreReturnValue
   @Override
   public boolean put(@ParametricNullness K key, @ParametricNullness V value) {
      return this.delegate().put(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean putAll(@ParametricNullness K key, Iterable<? extends V> values) {
      return this.delegate().putAll(key, values);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
      return this.delegate().putAll(multimap);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean remove(@Nullable Object key, @Nullable Object value) {
      return this.delegate().remove(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public Collection<V> removeAll(@Nullable Object key) {
      return this.delegate().removeAll(key);
   }

   @CanIgnoreReturnValue
   @Override
   public Collection<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      return this.delegate().replaceValues(key, values);
   }

   @Override
   public int size() {
      return this.delegate().size();
   }

   @Override
   public Collection<V> values() {
      return this.delegate().values();
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return object == this || this.delegate().equals(object);
   }

   @Override
   public int hashCode() {
      return this.delegate().hashCode();
   }
}
