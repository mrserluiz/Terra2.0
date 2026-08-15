package org.incendo.cloud.key;

import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.STABLE)
public interface MutableCloudKeyContainer extends CloudKeyContainer {
   <V> void store(@NonNull CloudKey<V> key, V value);

   <V> void store(@NonNull String key, V value);

   default <V> void store(@NonNull CloudKeyHolder<V> keyHolder, V value) {
      this.store(keyHolder.key(), value);
   }

   void remove(@NonNull CloudKey<?> key);

   default void remove(final @NonNull String key) {
      this.remove(CloudKey.of(key));
   }

   default void remove(final @NonNull CloudKeyHolder<?> keyHolder) {
      this.remove(keyHolder.key());
   }

   default <V> void set(final @NonNull CloudKey<V> key, final @Nullable V value) {
      if (value == null) {
         this.remove(key);
      } else {
         this.store(key, value);
      }
   }

   default <V> void set(final @NonNull String key, final @Nullable V value) {
      if (value == null) {
         this.remove(key);
      } else {
         this.store(key, value);
      }
   }

   default <V> void set(final @NonNull CloudKeyHolder<V> keyHolder, final @Nullable V value) {
      if (value == null) {
         this.remove(keyHolder);
      } else {
         this.store(keyHolder, value);
      }
   }

   <V> V computeIfAbsent(@NonNull CloudKey<V> key, @NonNull Function<@NonNull CloudKey<V>, V> defaultFunction);

   default <V> V computeIfAbsent(final @NonNull CloudKeyHolder<V> keyHolder, final @NonNull Function<@NonNull CloudKey<V>, V> defaultFunction) {
      return this.computeIfAbsent(keyHolder.key(), defaultFunction);
   }
}
