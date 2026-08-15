package org.incendo.cloud.exception.handling;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(status = Status.STABLE)
public interface ExceptionHandler<C, T extends Throwable> {
   static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> noopHandler() {
      return ctx -> {};
   }

   static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> passThroughHandler() {
      return ctx -> {
         throw ctx.exception();
      };
   }

   static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> passThroughHandler(final @NonNull Consumer<ExceptionContext<C, T>> consumer) {
      return ctx -> {
         consumer.accept(ctx);
         throw ctx.exception();
      };
   }

   static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> unwrappingHandler(final @NonNull Predicate<Throwable> predicate) {
      return ctx -> {
         Throwable cause = ctx.exception().getCause();
         if (cause != null && predicate.test(cause)) {
            throw cause;
         } else {
            throw ctx.exception();
         }
      };
   }

   static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> unwrappingHandler(final @NonNull Class<? extends Throwable> causeClass) {
      return unwrappingHandler(causeClass::isInstance);
   }

   static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> unwrappingHandler() {
      return unwrappingHandler(throwable -> true);
   }

   void handle(@NonNull ExceptionContext<C, T> context) throws Throwable;
}
