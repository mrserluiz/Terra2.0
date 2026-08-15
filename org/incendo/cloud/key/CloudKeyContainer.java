package org.incendo.cloud.key;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface CloudKeyContainer {
   <V> @NonNull Optional<V> optional(@NonNull CloudKey<V> key);

   <V> @NonNull Optional<V> optional(@NonNull String key);

   default <V> @NonNull Optional<V> optional(@NonNull CloudKeyHolder<V> keyHolder) {
      return this.optional(keyHolder.key());
   }

   default <V> V getOrDefault(@NonNull CloudKey<@NonNull V> key, V defaultValue) {
      return this.optional(key).orElse(defaultValue);
   }

   default <V> V getOrDefault(@NonNull String key, V defaultValue) {
      return this.<V>optional(key).orElse(defaultValue);
   }

   default <V> V getOrDefault(@NonNull CloudKeyHolder<@NonNull V> keyHolder, V defaultValue) {
      return this.getOrDefault(keyHolder.key(), defaultValue);
   }

   default <V> V getOrSupplyDefault(@NonNull CloudKey<@NonNull V> key, @NonNull Supplier<V> supplier) {
      return this.optional(key).orElseGet(supplier);
   }

   default <V> V getOrSupplyDefault(@NonNull String key, @NonNull Supplier<V> supplier) {
      return this.<V>optional(key).orElseGet(supplier);
   }

   default <V> V getOrSupplyDefault(@NonNull CloudKeyHolder<@NonNull V> keyHolder, @NonNull Supplier<V> supplier) {
      return this.optional(keyHolder).orElseGet(supplier);
   }

   default <V> V get(@NonNull CloudKey<V> key) {
      return this.optional(key)
         .orElseThrow(() -> new NullPointerException(String.format("There is no object in the registry identified by the key '%s'", key.name())));
   }

   default <V> V get(@NonNull String key) {
      return this.<Object>optional(key)
         .map(value -> (V)value)
         .orElseThrow(() -> new NullPointerException(String.format("There is no object in the registry identified by the key '%s'", key)));
   }

   default <V> V get(@NonNull CloudKeyHolder<V> keyHolder) {
      return this.get(keyHolder.key());
   }

   boolean contains(@NonNull CloudKey<?> key);

   default boolean contains(@NonNull String key) {
      return this.contains(CloudKey.of(key));
   }

   default boolean contains(@NonNull CloudKeyHolder<?> keyHolder) {
      return this.contains(keyHolder.key());
   }

   @NonNull Map<CloudKey<?>, ? extends @NonNull Object> all();
}
