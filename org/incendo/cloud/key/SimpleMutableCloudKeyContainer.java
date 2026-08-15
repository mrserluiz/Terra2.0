package org.incendo.cloud.key;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.INTERNAL)
public final class SimpleMutableCloudKeyContainer implements MutableCloudKeyContainer {
   private final Map<CloudKey<?>, Object> map;

   public SimpleMutableCloudKeyContainer(final Map<CloudKey<?>, Object> map) {
      this.map = map;
   }

   @Override
   public <V> @NonNull Optional<V> optional(final @NonNull CloudKey<V> key) {
      return Optional.ofNullable((V)this.map.get(key));
   }

   @Override
   public <V> @NonNull Optional<V> optional(final @NonNull String key) {
      return this.optional((CloudKey<V>)CloudKey.of(key));
   }

   @Override
   public boolean contains(final @NonNull CloudKey<?> key) {
      return this.map.containsKey(key);
   }

   @Override
   public @NonNull Map<CloudKey<?>, ? extends @NonNull Object> all() {
      return Collections.unmodifiableMap(this.map);
   }

   @Override
   public <V> void store(final @NonNull CloudKey<V> key, final @NonNull V value) {
      this.map.put(key, value);
   }

   @Override
   public <V> void store(final @NonNull String key, final @NonNull V value) {
      this.map.put(CloudKey.of(key), value);
   }

   @Override
   public void remove(final @NonNull CloudKey<?> key) {
      this.map.remove(key);
   }

   @Override
   public <V> V computeIfAbsent(final @NonNull CloudKey<V> key, final @NonNull Function<@NonNull CloudKey<V>, V> defaultFunction) {
      return (V)this.map.computeIfAbsent(key, $ -> defaultFunction.apply(key));
   }

   public <V> @Nullable V getOrNull(final CloudKey<V> key) {
      return (V)this.map.get(key);
   }
}
