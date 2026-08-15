package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Objects;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractMapEntry<K, V> implements Entry<K, V> {
   @ParametricNullness
   @Override
   public abstract K getKey();

   @ParametricNullness
   @Override
   public abstract V getValue();

   @ParametricNullness
   @Override
   public V setValue(@ParametricNullness V value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (!(object instanceof Entry)) {
         return false;
      }

      Entry<?, ?> that = (Entry<?, ?>)object;
      return Objects.equal(this.getKey(), that.getKey()) && Objects.equal(this.getValue(), that.getValue());
   }

   @Override
   public int hashCode() {
      K k = this.getKey();
      V v = this.getValue();
      return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
   }

   @Override
   public String toString() {
      return this.getKey() + "=" + this.getValue();
   }
}
