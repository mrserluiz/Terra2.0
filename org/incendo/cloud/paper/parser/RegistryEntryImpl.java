package org.incendo.cloud.paper.parser;

import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.NamespacedKey;
import org.immutables.value.Generated;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "RegistryEntryParser.RegistryEntry", generator = "Immutables")
@Immutable
final class RegistryEntryImpl<E> implements RegistryEntryParser.RegistryEntry<E> {
   private final E value;
   private final NamespacedKey key;

   private RegistryEntryImpl(E value, NamespacedKey key) {
      this.value = Objects.requireNonNull(value, "value");
      this.key = Objects.requireNonNull(key, "key");
   }

   private RegistryEntryImpl(RegistryEntryImpl<E> original, E value, NamespacedKey key) {
      this.value = value;
      this.key = key;
   }

   @Override
   public E value() {
      return this.value;
   }

   @Override
   public NamespacedKey key() {
      return this.key;
   }

   public final RegistryEntryImpl<E> withValue(E value) {
      if (this.value == value) {
         return this;
      }

      E newValue = Objects.requireNonNull(value, "value");
      return new RegistryEntryImpl<>(this, newValue, this.key);
   }

   public final RegistryEntryImpl<E> withKey(NamespacedKey value) {
      if (this.key == value) {
         return this;
      }

      NamespacedKey newValue = Objects.requireNonNull(value, "key");
      return new RegistryEntryImpl<>(this, this.value, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof RegistryEntryImpl && this.equalsByValue((RegistryEntryImpl<?>)another);
   }

   private boolean equalsByValue(RegistryEntryImpl<?> another) {
      return this.value.equals(another.value) && this.key.equals(another.key);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.value.hashCode();
      return h + (h << 5) + this.key.hashCode();
   }

   @Override
   public String toString() {
      return "RegistryEntry{value=" + this.value + ", key=" + this.key + "}";
   }

   public static <E> RegistryEntryImpl<E> of(E value, NamespacedKey key) {
      return new RegistryEntryImpl<>(value, key);
   }

   public static <E> RegistryEntryImpl<E> copyOf(RegistryEntryParser.RegistryEntry<E> instance) {
      return instance instanceof RegistryEntryImpl ? (RegistryEntryImpl)instance : of(instance.value(), instance.key());
   }
}
