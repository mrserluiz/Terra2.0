package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.Serializable;

@GwtCompatible(serializable = true)
class ImmutableEntry<K, V> extends AbstractMapEntry<K, V> implements Serializable {
   @ParametricNullness
   final K key;
   @ParametricNullness
   final V value;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   ImmutableEntry(@ParametricNullness K key, @ParametricNullness V value) {
      this.key = key;
      this.value = value;
   }

   @ParametricNullness
   @Override
   public final K getKey() {
      return this.key;
   }

   @ParametricNullness
   @Override
   public final V getValue() {
      return this.value;
   }

   @ParametricNullness
   @Override
   public final V setValue(@ParametricNullness V value) {
      throw new UnsupportedOperationException();
   }
}
