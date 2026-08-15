package org.incendo.cloud.parser.flag;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.component.TypedCommandComponent;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.permission.Permission;

@API(status = Status.STABLE)
public final class CommandFlag<T> {
   private final @NonNull String name;
   private final @NonNull String @NonNull [] aliases;
   private final @NonNull Description description;
   private final @NonNull Permission permission;
   private final CommandFlag.@NonNull FlagMode mode;
   private final @Nullable TypedCommandComponent<?, T> commandComponent;

   private CommandFlag(
      final @NonNull String name,
      final @NonNull String @NonNull [] aliases,
      final @NonNull Description description,
      final @NonNull Permission permission,
      final @Nullable TypedCommandComponent<?, T> commandComponent,
      final CommandFlag.@NonNull FlagMode mode
   ) {
      this.name = Objects.requireNonNull(name, "name cannot be null");
      this.aliases = Objects.requireNonNull(aliases, "aliases cannot be null");
      this.description = Objects.requireNonNull(description, "description cannot be null");
      this.permission = Objects.requireNonNull(permission, "permission cannot be null");
      this.commandComponent = commandComponent;
      this.mode = Objects.requireNonNull(mode, "mode cannot be null");
   }

   @API(status = Status.STABLE)
   public static <C> CommandFlag.@NonNull Builder<C, Void> builder(final @NonNull String name) {
      return new CommandFlag.Builder<>(name);
   }

   public @NonNull String name() {
      return this.name;
   }

   public @NonNull Collection<@NonNull String> aliases() {
      return Arrays.asList(this.aliases);
   }

   @API(status = Status.STABLE)
   public CommandFlag.@NonNull FlagMode mode() {
      return this.mode;
   }

   @API(status = Status.STABLE)
   public @NonNull Description description() {
      return this.description;
   }

   @API(status = Status.STABLE)
   public @Nullable CommandComponent<?> commandComponent() {
      return this.commandComponent;
   }

   @API(status = Status.STABLE)
   public Permission permission() {
      return this.permission;
   }

   @Override
   public String toString() {
      return String.format("--%s", this.name);
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CommandFlag<?> that = (CommandFlag<?>)o;
         return this.name().equals(that.name());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name());
   }

   @API(status = Status.STABLE)
   public static final class Builder<C, T> {
      private final String name;
      private final String[] aliases;
      private final Description description;
      private final Permission permission;
      private final TypedCommandComponent<C, T> commandComponent;
      private final CommandFlag.FlagMode mode;

      private Builder(
         final @NonNull String name,
         final @NonNull String[] aliases,
         final @NonNull Description description,
         final @NonNull Permission permission,
         final @Nullable TypedCommandComponent<C, T> commandComponent,
         final CommandFlag.@NonNull FlagMode mode
      ) {
         this.name = name;
         this.aliases = aliases;
         this.description = description;
         this.permission = permission;
         this.commandComponent = commandComponent;
         this.mode = mode;
      }

      private Builder(final @NonNull String name) {
         this(name, new String[0], Description.empty(), Permission.empty(), null, CommandFlag.FlagMode.SINGLE);
      }

      public CommandFlag.@NonNull Builder<C, T> withAliases(final @NonNull String... aliases) {
         return this.withAliases(Arrays.asList(aliases));
      }

      @API(status = Status.STABLE)
      public CommandFlag.@NonNull Builder<C, T> withAliases(final @NonNull Collection<@NonNull String> aliases) {
         Set<String> filteredAliases = new HashSet<>();

         for (String alias : aliases) {
            if (!alias.isEmpty()) {
               if (alias.length() > 1) {
                  throw new IllegalArgumentException(String.format("Alias '%s' has name longer than one character. This is not allowed", alias));
               }

               filteredAliases.add(alias);
            }
         }

         return new CommandFlag.Builder<>(
            this.name, filteredAliases.toArray(new String[0]), this.description, this.permission, this.commandComponent, this.mode
         );
      }

      @API(status = Status.STABLE)
      public CommandFlag.@NonNull Builder<C, T> withDescription(final @NonNull Description description) {
         return new CommandFlag.Builder<>(this.name, this.aliases, description, this.permission, this.commandComponent, this.mode);
      }

      public <N> CommandFlag.@NonNull Builder<C, N> withComponent(final @NonNull TypedCommandComponent<C, N> component) {
         return new CommandFlag.Builder<>(this.name, this.aliases, this.description, this.permission, component, this.mode);
      }

      public <N> CommandFlag.@NonNull Builder<C, N> withComponent(final @NonNull ParserDescriptor<? super C, N> parserDescriptor) {
         return this.withComponent(CommandComponent.builder(this.name, parserDescriptor));
      }

      public <N> CommandFlag.@NonNull Builder<C, N> withComponent(final CommandComponent.@NonNull Builder<C, N> builder) {
         return this.withComponent(builder.build());
      }

      @API(status = Status.STABLE)
      public CommandFlag.@NonNull Builder<C, T> withPermission(final @NonNull Permission permission) {
         return new CommandFlag.Builder<>(this.name, this.aliases, this.description, permission, this.commandComponent, this.mode);
      }

      @API(status = Status.STABLE)
      public CommandFlag.@NonNull Builder<C, T> withPermission(final @NonNull String permissionString) {
         return this.withPermission(Permission.of(permissionString));
      }

      @API(status = Status.STABLE)
      public CommandFlag.@NonNull Builder<C, T> asRepeatable() {
         return new CommandFlag.Builder<>(this.name, this.aliases, this.description, this.permission, this.commandComponent, CommandFlag.FlagMode.REPEATABLE);
      }

      public @NonNull CommandFlag<T> build() {
         return new CommandFlag<>(this.name, this.aliases, this.description, this.permission, this.commandComponent, this.mode);
      }
   }

   @API(status = Status.STABLE)
   public enum FlagMode {
      SINGLE,
      REPEATABLE;
   }
}
