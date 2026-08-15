package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingMap<K, V> extends ForwardingObject implements Map<K, V> {
   protected ForwardingMap() {
   }

   protected abstract Map<K, V> delegate();

   @Override
   public int size() {
      return this.delegate().size();
   }

   @Override
   public boolean isEmpty() {
      return this.delegate().isEmpty();
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V remove(@Nullable Object key) {
      return this.delegate().remove(key);
   }

   @Override
   public void clear() {
      this.delegate().clear();
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
   public @Nullable V get(@Nullable Object key) {
      return this.delegate().get(key);
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V put(@ParametricNullness K key, @ParametricNullness V value) {
      return this.delegate().put(key, value);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> map) {
      this.delegate().putAll(map);
   }

   @Override
   public Set<K> keySet() {
      return this.delegate().keySet();
   }

   @Override
   public Collection<V> values() {
      return this.delegate().values();
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      return this.delegate().entrySet();
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return object == this || this.delegate().equals(object);
   }

   @Override
   public int hashCode() {
      return this.delegate().hashCode();
   }

   protected void standardPutAll(Map<? extends K, ? extends V> map) {
      Maps.putAllImpl(this, map);
   }

   protected @Nullable V standardRemove(@Nullable Object key) {
      Iterator<Entry<K, V>> entryIterator = this.entrySet().iterator();

      while (entryIterator.hasNext()) {
         Entry<K, V> entry = entryIterator.next();
         if (Objects.equal(entry.getKey(), key)) {
            V value = entry.getValue();
            entryIterator.remove();
            return value;
         }
      }

      return null;
   }

   protected void standardClear() {
      Iterators.clear(this.entrySet().iterator());
   }

   protected boolean standardContainsKey(@Nullable Object key) {
      return Maps.containsKeyImpl(this, key);
   }

   protected boolean standardContainsValue(@Nullable Object value) {
      return Maps.containsValueImpl(this, value);
   }

   protected boolean standardIsEmpty() {
      return !this.entrySet().iterator().hasNext();
   }

   protected boolean standardEquals(@Nullable Object object) {
      return Maps.equalsImpl(this, object);
   }

   protected int standardHashCode() {
      return Sets.hashCodeImpl(this.entrySet());
   }

   protected String standardToString() {
      return Maps.toStringImpl(this);
   }

   protected abstract class StandardEntrySet extends Maps.EntrySet<K, V> {
      @Override
      Map<K, V> map() {
         return ForwardingMap.this;
      }
   }

   protected class StandardKeySet extends Maps.KeySet<K, V> {
      public StandardKeySet() {
         super(ForwardingMap.this);
      }
   }

   protected class StandardValues extends Maps.Values<K, V> {
      public StandardValues() {
         super(ForwardingMap.this);
      }
   }
}
