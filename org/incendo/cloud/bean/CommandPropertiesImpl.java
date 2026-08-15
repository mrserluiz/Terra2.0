package org.incendo.cloud.bean;

import java.util.Collection;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "CommandProperties", generator = "Immutables")
@Immutable
final class CommandPropertiesImpl implements CommandProperties {
   private final @NonNull String name;
   private final @NonNull Collection<String> aliases;

   private CommandPropertiesImpl(@NonNull String name, @NonNull Collection<String> aliases) {
      this.name = Objects.requireNonNull(name, "name");
      this.aliases = Objects.requireNonNull(aliases, "aliases");
   }

   private CommandPropertiesImpl(CommandPropertiesImpl original, @NonNull String name, @NonNull Collection<String> aliases) {
      this.name = name;
      this.aliases = aliases;
   }

   @Override
   public @NonNull String name() {
      return this.name;
   }

   @Override
   public @NonNull Collection<String> aliases() {
      return this.aliases;
   }

   public final CommandPropertiesImpl withName(String value) {
      String newValue = Objects.requireNonNull(value, "name");
      return this.name.equals(newValue) ? this : new CommandPropertiesImpl(this, newValue, this.aliases);
   }

   public final CommandPropertiesImpl withAliases(Collection<String> value) {
      if (this.aliases == value) {
         return this;
      }

      Collection<String> newValue = Objects.requireNonNull(value, "aliases");
      return new CommandPropertiesImpl(this, this.name, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof CommandPropertiesImpl && this.equalTo(0, (CommandPropertiesImpl)another);
   }

   private boolean equalTo(int synthetic, CommandPropertiesImpl another) {
      return this.name.equals(another.name) && this.aliases.equals(another.aliases);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.name.hashCode();
      return h + (h << 5) + this.aliases.hashCode();
   }

   @Override
   public String toString() {
      return "CommandProperties{name=" + this.name + ", aliases=" + this.aliases + "}";
   }

   public static CommandPropertiesImpl of(@NonNull String name, @NonNull Collection<String> aliases) {
      return new CommandPropertiesImpl(name, aliases);
   }

   public static CommandPropertiesImpl copyOf(CommandProperties instance) {
      return instance instanceof CommandPropertiesImpl ? (CommandPropertiesImpl)instance : of(instance.name(), instance.aliases());
   }
}
