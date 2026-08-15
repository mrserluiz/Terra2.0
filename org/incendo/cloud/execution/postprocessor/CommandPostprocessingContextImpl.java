package org.incendo.cloud.execution.postprocessor;

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
import org.incendo.cloud.context.CommandContext;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "CommandPostprocessingContext", generator = "Immutables")
@Immutable
final class CommandPostprocessingContextImpl<C> implements CommandPostprocessingContext<C> {
   private final @NonNull CommandContext<C> commandContext;
   private final @NonNull Command<C> command;

   private CommandPostprocessingContextImpl(@NonNull CommandContext<C> commandContext, @NonNull Command<C> command) {
      this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
      this.command = Objects.requireNonNull(command, "command");
   }

   private CommandPostprocessingContextImpl(
      CommandPostprocessingContextImpl<C> original, @NonNull CommandContext<C> commandContext, @NonNull Command<C> command
   ) {
      this.commandContext = commandContext;
      this.command = command;
   }

   @Override
   public @NonNull CommandContext<C> commandContext() {
      return this.commandContext;
   }

   @Override
   public @NonNull Command<C> command() {
      return this.command;
   }

   public final CommandPostprocessingContextImpl<C> withCommandContext(CommandContext<C> value) {
      if (this.commandContext == value) {
         return this;
      }

      CommandContext<C> newValue = Objects.requireNonNull(value, "commandContext");
      return new CommandPostprocessingContextImpl<>(this, newValue, this.command);
   }

   public final CommandPostprocessingContextImpl<C> withCommand(Command<C> value) {
      if (this.command == value) {
         return this;
      }

      Command<C> newValue = Objects.requireNonNull(value, "command");
      return new CommandPostprocessingContextImpl<>(this, this.commandContext, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof CommandPostprocessingContextImpl && this.equalTo(0, (CommandPostprocessingContextImpl<?>)another);
   }

   private boolean equalTo(int synthetic, CommandPostprocessingContextImpl<?> another) {
      return this.commandContext.equals(another.commandContext) && this.command.equals(another.command);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.commandContext.hashCode();
      return h + (h << 5) + this.command.hashCode();
   }

   @Override
   public String toString() {
      return "CommandPostprocessingContext{commandContext=" + this.commandContext + ", command=" + this.command + "}";
   }

   public static <C> CommandPostprocessingContextImpl<C> of(@NonNull CommandContext<C> commandContext, @NonNull Command<C> command) {
      return new CommandPostprocessingContextImpl<>(commandContext, command);
   }

   public static <C> CommandPostprocessingContextImpl<C> copyOf(CommandPostprocessingContext<C> instance) {
      return instance instanceof CommandPostprocessingContextImpl
         ? (CommandPostprocessingContextImpl)instance
         : of(instance.commandContext(), instance.command());
   }
}
