package org.incendo.cloud.help.result;

import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;
import org.incendo.cloud.Command;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "CommandEntry", generator = "Immutables")
@Immutable
final class CommandEntryImpl<C> implements CommandEntry<C> {
   private final @NonNull Command<C> command;
   private final @NonNull String syntax;

   private CommandEntryImpl(@NonNull Command<C> command, @NonNull String syntax) {
      this.command = Objects.requireNonNull(command, "command");
      this.syntax = Objects.requireNonNull(syntax, "syntax");
   }

   private CommandEntryImpl(CommandEntryImpl<C> original, @NonNull Command<C> command, @NonNull String syntax) {
      this.command = command;
      this.syntax = syntax;
   }

   @Override
   public @NonNull Command<C> command() {
      return this.command;
   }

   @Override
   public @NonNull String syntax() {
      return this.syntax;
   }

   public final CommandEntryImpl<C> withCommand(Command<C> value) {
      if (this.command == value) {
         return this;
      }

      Command<C> newValue = Objects.requireNonNull(value, "command");
      return new CommandEntryImpl<>(this, newValue, this.syntax);
   }

   public final CommandEntryImpl<C> withSyntax(String value) {
      String newValue = Objects.requireNonNull(value, "syntax");
      return this.syntax.equals(newValue) ? this : new CommandEntryImpl<>(this, this.command, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof CommandEntryImpl && this.equalTo(0, (CommandEntryImpl<?>)another);
   }

   private boolean equalTo(int synthetic, CommandEntryImpl<?> another) {
      return this.command.equals(another.command) && this.syntax.equals(another.syntax);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.command.hashCode();
      return h + (h << 5) + this.syntax.hashCode();
   }

   @Override
   public String toString() {
      return "CommandEntry{command=" + this.command + ", syntax=" + this.syntax + "}";
   }

   public static <C> CommandEntryImpl<C> of(@NonNull Command<C> command, @NonNull String syntax) {
      return new CommandEntryImpl<>(command, syntax);
   }

   public static <C> CommandEntryImpl<C> copyOf(CommandEntry<C> instance) {
      return instance instanceof CommandEntryImpl ? (CommandEntryImpl)instance : of(instance.command(), instance.syntax());
   }
}
