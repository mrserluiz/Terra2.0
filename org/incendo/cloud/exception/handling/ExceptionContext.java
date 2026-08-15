package org.incendo.cloud.exception.handling;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
public interface ExceptionContext<C, T extends Throwable> {
   @NonNull T exception();

   @NonNull CommandContext<C> context();

   @NonNull ExceptionController<C> controller();

   @API(status = Status.INTERNAL)
   final class ExceptionContextImpl<C, T extends Throwable> implements ExceptionContext<C, T> {
      private final T exception;
      private final CommandContext<C> context;
      private final ExceptionController<C> controller;

      ExceptionContextImpl(final @NonNull T exception, final @NonNull CommandContext<C> context, final @NonNull ExceptionController<C> controller) {
         this.exception = exception;
         this.context = context;
         this.controller = controller;
      }

      @Override
      public @NonNull T exception() {
         return this.exception;
      }

      @Override
      public @NonNull CommandContext<C> context() {
         return this.context;
      }

      @Override
      public @NonNull ExceptionController<C> controller() {
         return this.controller;
      }

      @Override
      public boolean equals(final Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            ExceptionContext.ExceptionContextImpl<?, ?> that = (ExceptionContext.ExceptionContextImpl<?, ?>)object;
            return Objects.equals(this.exception, that.exception)
               && Objects.equals(this.context, that.context)
               && Objects.equals(this.controller, that.controller);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.exception, this.context, this.controller);
      }
   }
}
