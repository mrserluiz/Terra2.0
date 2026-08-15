package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingConcurrentMap<K, V> extends ForwardingMap<K, V> implements ConcurrentMap<K, V> {
   protected ForwardingConcurrentMap() {
   }

   protected abstract ConcurrentMap<K, V> delegate();

   @CanIgnoreReturnValue
   @Override
   public @Nullable V putIfAbsent(K key, V value) {
      return this.delegate().putIfAbsent(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean remove(@Nullable Object key, @Nullable Object value) {
      return this.delegate().remove(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V replace(K key, V value) {
      return this.delegate().replace(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean replace(K key, V oldValue, V newValue) {
      return this.delegate().replace(key, oldValue, newValue);
   }
}
