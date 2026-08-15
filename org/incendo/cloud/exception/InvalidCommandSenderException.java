package org.incendo.cloud.exception;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.util.TypeUtils;

@API(status = Status.STABLE)
public final class InvalidCommandSenderException extends CommandParseException {
   private final Set<Type> requiredSenderTypes;
   private final @Nullable Command<?> command;

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public InvalidCommandSenderException(
      final @NonNull Object commandSender,
      final @NonNull Type requiredSenderTypes,
      final @NonNull List<@NonNull CommandComponent<?>> currentChain,
      final @Nullable Command<?> command
   ) {
      this(commandSender, new HashSet<>(Collections.singletonList(requiredSenderTypes)), currentChain, command);
   }

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public InvalidCommandSenderException(
      final @NonNull Object commandSender,
      final @NonNull Set<Type> requiredSenderTypes,
      final @NonNull List<@NonNull CommandComponent<?>> currentChain,
      final @Nullable Command<?> command
   ) {
      super(commandSender, currentChain);
      this.requiredSenderTypes = Collections.unmodifiableSet(requiredSenderTypes);
      this.command = command;
   }

   public @NonNull Set<Type> requiredSenderTypes() {
      return this.requiredSenderTypes;
   }

   @Override
   public String getMessage() {
      return this.requiredSenderTypes.size() == 1
         ? String.format(
            "%s is not allowed to execute that command. Must be of type %s",
            this.commandSender().getClass().getSimpleName(),
            TypeUtils.simpleName(this.requiredSenderTypes.iterator().next())
         )
         : String.format(
            "%s is not allowed to execute that command. Must be one of %s",
            this.commandSender().getClass().getSimpleName(),
            this.requiredSenderTypes.stream().map(TypeUtils::simpleName).collect(Collectors.joining(", "))
         );
   }

   @API(status = Status.STABLE)
   public @Nullable Command<?> command() {
      return this.command;
   }
}
