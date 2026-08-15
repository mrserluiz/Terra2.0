package org.incendo.cloud.meta;

import io.leangen.geantyref.GenericTypeReflector;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.key.CloudKey;

@API(status = Status.STABLE)
public class SimpleCommandMeta extends CommandMeta {
   private final Map<CloudKey<?>, Object> metaMap;

   protected SimpleCommandMeta(final @NonNull Map<@NonNull CloudKey<?>, @NonNull Object> metaMap) {
      this.metaMap = Collections.unmodifiableMap(new HashMap<>(metaMap));
   }

   @Override
   public final <V> @NonNull Optional<V> optional(final @NonNull CloudKey<V> key) {
      Object value = this.metaMap.get(key);
      if (value == null) {
         return Optional.empty();
      } else if (!GenericTypeReflector.isSuperType(key.type().getType(), value.getClass())) {
         throw new IllegalArgumentException(
            "Conflicting argument types between key type of " + key.type().getType().getTypeName() + " and value type of " + value.getClass()
         );
      } else {
         return Optional.of((V)value);
      }
   }

   @Override
   public <V> @NonNull Optional<V> optional(final @NonNull String key) {
      Object value = this.metaMap.get(CloudKey.of(key));
      return value == null ? Optional.empty() : Optional.of((V)value);
   }

   @Override
   public boolean contains(final @NonNull CloudKey<?> key) {
      return this.metaMap.containsKey(key);
   }

   @Override
   public final @NonNull Map<CloudKey<?>, ? extends @NonNull Object> all() {
      return new HashMap<>(this.metaMap);
   }

   @Override
   public final boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         SimpleCommandMeta that = (SimpleCommandMeta)other;
         return Objects.equals(this.metaMap, that.metaMap);
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hashCode(this.metaMap);
   }
}
