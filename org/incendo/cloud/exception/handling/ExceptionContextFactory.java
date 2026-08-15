package org.incendo.cloud.exception.handling;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.INTERNAL)
public final class ExceptionContextFactory<C> {
   private final ExceptionController<C> controller;

   public ExceptionContextFactory(final @NonNull ExceptionController<C> controller) {
      this.controller = controller;
   }

   public <T extends Throwable> @NonNull ExceptionContext<C, T> createContext(final @NonNull CommandContext<C> context, final @NonNull T exception) {
      return new ExceptionContext.ExceptionContextImpl<>(exception, context, this.controller);
   }
}
