package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CompatibleWith;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use CacheBuilder.newBuilder().build()")
@GwtCompatible
public interface Cache<K, V> {
   @CanIgnoreReturnValue
   @Nullable V getIfPresent(@CompatibleWith("K") Object key);

   @CanIgnoreReturnValue
   V get(K key, Callable<? extends V> loader) throws ExecutionException;

   ImmutableMap<K, V> getAllPresent(Iterable<? extends Object> keys);

   void put(K key, V value);

   void putAll(Map<? extends K, ? extends V> m);

   void invalidate(@CompatibleWith("K") Object key);

   void invalidateAll(Iterable<? extends Object> keys);

   void invalidateAll();

   long size();

   CacheStats stats();

   ConcurrentMap<K, V> asMap();

   void cleanUp();
}
