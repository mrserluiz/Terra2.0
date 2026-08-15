package org.incendo.cloud.execution.preprocessor;

import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "CommandPreprocessingContext", generator = "Immutables")
@Immutable
final class CommandPreprocessingContextImpl<C> implements CommandPreprocessingContext<C> {
   private final @NonNull CommandContext<C> commandContext;
   private final @NonNull CommandInput commandInput;

   private CommandPreprocessingContextImpl(@NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput) {
      this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
      this.commandInput = Objects.requireNonNull(commandInput, "commandInput");
   }

   private CommandPreprocessingContextImpl(
      CommandPreprocessingContextImpl<C> original, @NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput
   ) {
      this.commandContext = commandContext;
      this.commandInput = commandInput;
   }

   @Override
   public @NonNull CommandContext<C> commandContext() {
      return this.commandContext;
   }

   @Override
   public @NonNull CommandInput commandInput() {
      return this.commandInput;
   }

   public final CommandPreprocessingContextImpl<C> withCommandContext(CommandContext<C> value) {
      if (this.commandContext == value) {
         return this;
      }

      CommandContext<C> newValue = Objects.requireNonNull(value, "commandContext");
      return new CommandPreprocessingContextImpl<>(this, newValue, this.commandInput);
   }

   public final CommandPreprocessingContextImpl<C> withCommandInput(CommandInput value) {
      if (this.commandInput == value) {
         return this;
      }

      CommandInput newValue = Objects.requireNonNull(value, "commandInput");
      return new CommandPreprocessingContextImpl<>(this, this.commandContext, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof CommandPreprocessingContextImpl && this.equalTo(0, (CommandPreprocessingContextImpl<?>)another);
   }

   private boolean equalTo(int synthetic, CommandPreprocessingContextImpl<?> another) {
      return this.commandContext.equals(another.commandContext) && this.commandInput.equals(another.commandInput);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.commandContext.hashCode();
      return h + (h << 5) + this.commandInput.hashCode();
   }

   @Override
   public String toString() {
      return "CommandPreprocessingContext{commandContext=" + this.commandContext + ", commandInput=" + this.commandInput + "}";
   }

   public static <C> CommandPreprocessingContextImpl<C> of(@NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput) {
      return new CommandPreprocessingContextImpl<>(commandContext, commandInput);
   }

   public static <C> CommandPreprocessingContextImpl<C> copyOf(CommandPreprocessingContext<C> instance) {
      return instance instanceof CommandPreprocessingContextImpl
         ? (CommandPreprocessingContextImpl)instance
         : of(instance.commandContext(), instance.commandInput());
   }
}
