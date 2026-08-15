package org.incendo.cloud.exception.handling;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
public final class ExceptionController<C> {
   private final ExceptionContextFactory<C> exceptionContextFactory = new ExceptionContextFactory<>(this);
   private final Map<@NonNull Type, @NonNull LinkedList<@NonNull ExceptionHandlerRegistration<C, ?>>> registrations = new HashMap<>();

   public static @NonNull Throwable unwrapCompletionException(final @NonNull Throwable throwable) {
      return throwable instanceof CompletionException ? unwrapCompletionException(throwable.getCause()) : throwable;
   }

   public <T extends Throwable> void handleException(final @NonNull CommandContext<C> commandContext, final @NonNull T exception) throws Throwable {
      ExceptionContext<C, T> exceptionContext = this.exceptionContextFactory.createContext(commandContext, exception);

      for (Class<?> exceptionClass = exception.getClass(); exceptionClass != Object.class; exceptionClass = exceptionClass.getSuperclass()) {
         for (ExceptionHandlerRegistration<C, ?> registration : this.registrations(exceptionClass)) {
            if (((Predicate<T>)registration.exceptionFilter()).test(exception)) {
               try {
                  registration.exceptionHandler().handle(exceptionContext);
                  return;
               } catch (Throwable throwable) {
                  if (!throwable.equals(exception)) {
                     this.handleException(commandContext, throwable);
                     return;
                  }
               }
            }
         }
      }

      throw exception;
   }

   public synchronized <T extends Throwable> @This @NonNull ExceptionController<C> register(
      final @NonNull ExceptionHandlerRegistration<C, ? extends T> registration
   ) {
      this.registrations.computeIfAbsent(registration.exceptionType().getType(), t -> new LinkedList<>()).addFirst(registration);
      return this;
   }

   public <T extends Throwable> @This @NonNull ExceptionController<C> register(
      final @NonNull TypeToken<T> exceptionType, final ExceptionHandlerRegistration.@NonNull BuilderDecorator<C, T> decorator
   ) {
      return this.register(decorator.decorate(ExceptionHandlerRegistration.builder(exceptionType)).build());
   }

   public <T extends Throwable> @This @NonNull ExceptionController<C> register(
      final @NonNull Class<T> exceptionType, final ExceptionHandlerRegistration.@NonNull BuilderDecorator<C, T> decorator
   ) {
      return this.register(decorator.decorate(ExceptionHandlerRegistration.builder(TypeToken.get(exceptionType))).build());
   }

   public <T extends Throwable> @This @NonNull ExceptionController<C> registerHandler(
      final @NonNull TypeToken<T> exceptionType, final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler
   ) {
      return this.register(ExceptionHandlerRegistration.of(exceptionType, exceptionHandler));
   }

   public <T extends Throwable> @This @NonNull ExceptionController<C> registerHandler(
      final @NonNull Class<T> exceptionType, final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler
   ) {
      return this.register(ExceptionHandlerRegistration.of(TypeToken.get(exceptionType), exceptionHandler));
   }

   public void clearHandlers() {
      this.registrations.clear();
   }

   private @NonNull List<@NonNull ExceptionHandlerRegistration<C, ?>> registrations(final @NonNull Type type) {
      return Collections.unmodifiableList(this.registrations.getOrDefault(type, new LinkedList<>()));
   }
}
