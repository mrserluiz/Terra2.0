package com.github.benmanes.caffeine.cache;

import java.time.Duration;
import java.util.function.BiFunction;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Expiry<K, V> {
   long expireAfterCreate(K key, V value, long currentTime);

   long expireAfterUpdate(K key, V value, long currentTime, long currentDuration);

   long expireAfterRead(K key, V value, long currentTime, long currentDuration);

   static <K, V> Expiry<K, V> creating(BiFunction<K, V, Duration> function) {
      return new ExpiryAfterCreate<>(function);
   }

   static <K, V> Expiry<K, V> writing(BiFunction<K, V, Duration> function) {
      return new ExpiryAfterWrite<>(function);
   }

   static <K, V> Expiry<K, V> accessing(BiFunction<K, V, Duration> function) {
      return new ExpiryAfterAccess<>(function);
   }
}
