package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface Weigher<K, V> {
   int weigh(K key, V value);

   static <K, V> Weigher<K, V> singletonWeigher() {
      return SingletonWeigher.INSTANCE;
   }

   static <K, V> Weigher<K, V> boundedWeigher(Weigher<K, V> delegate) {
      return new BoundedWeigher<>(delegate);
   }
}
