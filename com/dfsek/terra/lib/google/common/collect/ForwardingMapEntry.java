package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingMapEntry<K, V> extends ForwardingObject implements Entry<K, V> {
   protected ForwardingMapEntry() {
   }

   protected abstract Entry<K, V> delegate();

   @ParametricNullness
   @Override
   public K getKey() {
      return this.delegate().getKey();
   }

   @ParametricNullness
   @Override
   public V getValue() {
      return this.delegate().getValue();
   }

   @ParametricNullness
   @CanIgnoreReturnValue
   @Override
   public V setValue(@ParametricNullness V value) {
      return this.delegate().setValue(value);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return this.delegate().equals(object);
   }

   @Override
   public int hashCode() {
      return this.delegate().hashCode();
   }

   protected boolean standardEquals(@Nullable Object object) {
      if (!(object instanceof Entry)) {
         return false;
      }

      Entry<?, ?> that = (Entry<?, ?>)object;
      return Objects.equal(this.getKey(), that.getKey()) && Objects.equal(this.getValue(), that.getValue());
   }

   protected int standardHashCode() {
      K k = this.getKey();
      V v = this.getValue();
      return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
   }

   protected String standardToString() {
      return this.getKey() + "=" + this.getValue();
   }
}
