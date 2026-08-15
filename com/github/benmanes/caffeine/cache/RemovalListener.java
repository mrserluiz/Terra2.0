package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface RemovalListener<K, V> {
   void onRemoval(@Nullable K key, @Nullable V value, RemovalCause cause);
}
