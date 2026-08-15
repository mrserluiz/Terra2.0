package org.incendo.cloud.parser.aggregate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.key.CloudKey;

final class AggregateParsingContextImpl<C> implements AggregateParsingContext<C> {
   private final Map<CloudKey<?>, Object> storage = new HashMap<>();
   private final Collection<@NonNull String> validKeys;

   AggregateParsingContextImpl(final @NonNull AggregateParser<C, ?> parser) {
      this.validKeys = parser.components().stream().map(CommandComponent::name).collect(Collectors.toList());
   }

   @Override
   public <V> void store(final @NonNull CloudKey<V> key, final @NonNull V value) {
      this.storage.put(key, value);
   }

   @Override
   public <V> void store(final @NonNull String key, final @NonNull V value) {
      this.storage.put(CloudKey.of(key), value);
   }

   @Override
   public void remove(final @NonNull CloudKey<?> key) {
      this.storage.remove(key);
   }

   @Override
   public <V> V computeIfAbsent(final @NonNull CloudKey<V> key, final @NonNull Function<@NonNull CloudKey<V>, V> defaultFunction) {
      return (V)this.storage.computeIfAbsent(key, k -> defaultFunction.apply((CloudKey<V>)k));
   }

   @Override
   public <V> @NonNull Optional<V> optional(final @NonNull CloudKey<V> key) {
      Object value = this.storage.get(key);
      if (value != null) {
         V castedValue = (V)value;
         return Optional.of(castedValue);
      } else {
         return Optional.empty();
      }
   }

   @Override
   public <V> @NonNull Optional<V> optional(final @NonNull String key) {
      Object value = this.storage.get(CloudKey.of(key));
      if (value != null) {
         V castedValue = (V)value;
         return Optional.of(castedValue);
      } else {
         return Optional.empty();
      }
   }

   @Override
   public <V> @NonNull V get(final @NonNull CloudKey<V> key) {
      if (!this.validKeys.contains(key.name())) {
         throw new NullPointerException("No value with the given key has been stored in the context");
      } else {
         return Objects.requireNonNull((V)this.storage.get(key));
      }
   }

   @Override
   public <V> @NonNull V get(final @NonNull String key) {
      if (!this.validKeys.contains(key)) {
         throw new NullPointerException("No value with the given key has been stored in the context");
      } else {
         return Objects.requireNonNull((V)this.storage.get(CloudKey.of(key)));
      }
   }

   @Override
   public boolean contains(final @NonNull CloudKey<?> key) {
      return this.storage.containsKey(key);
   }

   @Override
   public boolean contains(final @NonNull String key) {
      return this.storage.containsKey(CloudKey.of(key));
   }

   @Override
   public @NonNull Map<CloudKey<?>, ? extends @NonNull Object> all() {
      return this.storage;
   }
}
