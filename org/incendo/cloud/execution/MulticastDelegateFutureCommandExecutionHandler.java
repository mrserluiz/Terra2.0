package org.incendo.cloud.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.INTERNAL)
final class MulticastDelegateFutureCommandExecutionHandler<C> implements CommandExecutionHandler.FutureCommandExecutionHandler<C> {
   private final List<CommandExecutionHandler<C>> handlers;

   MulticastDelegateFutureCommandExecutionHandler(final @NonNull List<@NonNull CommandExecutionHandler<C>> handlers) {
      List<CommandExecutionHandler<C>> unwrappedHandlers = new ArrayList<>();

      for (CommandExecutionHandler<C> handler : handlers) {
         if (handler instanceof MulticastDelegateFutureCommandExecutionHandler) {
            unwrappedHandlers.addAll(((MulticastDelegateFutureCommandExecutionHandler)handler).handlers);
         } else {
            unwrappedHandlers.add(handler);
         }
      }

      this.handlers = Collections.unmodifiableList(unwrappedHandlers);
   }

   @Override
   public CompletableFuture<Void> executeFuture(final CommandContext<C> commandContext) {
      CompletableFuture<Void> composedHandler = null;
      if (this.handlers.isEmpty()) {
         composedHandler = CompletableFuture.completedFuture(null);
      } else {
         for (CommandExecutionHandler<C> handler : this.handlers) {
            if (composedHandler == null) {
               composedHandler = handler.executeFuture(commandContext);
            } else {
               composedHandler = composedHandler.thenCompose(ignore -> handler.executeFuture(commandContext));
            }
         }
      }

      return composedHandler;
   }
}
