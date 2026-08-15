package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public interface BiMap<K, V> extends Map<K, V> {
   @CanIgnoreReturnValue
   @Override
   @Nullable V put(@ParametricNullness K key, @ParametricNullness V value);

   @CanIgnoreReturnValue
   @Nullable V forcePut(@ParametricNullness K key, @ParametricNullness V value);

   @Override
   void putAll(Map<? extends K, ? extends V> map);

   Set<V> values();

   BiMap<V, K> inverse();
}
