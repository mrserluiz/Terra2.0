package com.dfsek.terra.lib.commons.lang3;

import com.dfsek.terra.lib.commons.lang3.exception.ExceptionUtils;
import com.dfsek.terra.lib.commons.lang3.function.Failable;
import com.dfsek.terra.lib.commons.lang3.function.FailableBooleanSupplier;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Deprecated
public class Functions {
   public static <O1, O2, T extends Throwable> void accept(Functions.FailableBiConsumer<O1, O2, T> consumer, O1 object1, O2 object2) {
      run(() -> consumer.accept(object1, object2));
   }

   public static <O, T extends Throwable> void accept(Functions.FailableConsumer<O, T> consumer, O object) {
      run(() -> consumer.accept(object));
   }

   public static <O1, O2, O, T extends Throwable> O apply(Functions.FailableBiFunction<O1, O2, O, T> function, O1 input1, O2 input2) {
      return get(() -> function.apply(input1, input2));
   }

   public static <I, O, T extends Throwable> O apply(Functions.FailableFunction<I, O, T> function, I input) {
      return get(() -> function.apply(input));
   }

   public static <O1, O2> BiConsumer<O1, O2> asBiConsumer(Functions.FailableBiConsumer<O1, O2, ?> consumer) {
      return (input1, input2) -> accept(consumer, input1, input2);
   }

   public static <O1, O2, O> BiFunction<O1, O2, O> asBiFunction(Functions.FailableBiFunction<O1, O2, O, ?> function) {
      return (input1, input2) -> apply(function, input1, input2);
   }

   public static <O1, O2> BiPredicate<O1, O2> asBiPredicate(Functions.FailableBiPredicate<O1, O2, ?> predicate) {
      return (input1, input2) -> test(predicate, input1, input2);
   }

   public static <O> Callable<O> asCallable(Functions.FailableCallable<O, ?> callable) {
      return () -> call(callable);
   }

   public static <I> Consumer<I> asConsumer(Functions.FailableConsumer<I, ?> consumer) {
      return input -> accept(consumer, input);
   }

   public static <I, O> Function<I, O> asFunction(Functions.FailableFunction<I, O, ?> function) {
      return input -> apply(function, input);
   }

   public static <I> Predicate<I> asPredicate(Functions.FailablePredicate<I, ?> predicate) {
      return input -> test(predicate, input);
   }

   public static Runnable asRunnable(Functions.FailableRunnable<?> runnable) {
      return () -> run(runnable);
   }

   public static <O> Supplier<O> asSupplier(Functions.FailableSupplier<O, ?> supplier) {
      return () -> get(supplier);
   }

   public static <O, T extends Throwable> O call(Functions.FailableCallable<O, T> callable) {
      return get(callable::call);
   }

   public static <O, T extends Throwable> O get(Functions.FailableSupplier<O, T> supplier) {
      try {
         return supplier.get();
      } catch (Throwable t) {
         throw rethrow(t);
      }
   }

   private static <T extends Throwable> boolean getAsBoolean(FailableBooleanSupplier<T> supplier) {
      try {
         return supplier.getAsBoolean();
      } catch (Throwable t) {
         throw rethrow(t);
      }
   }

   public static RuntimeException rethrow(Throwable throwable) {
      Objects.requireNonNull(throwable, "throwable");
      ExceptionUtils.throwUnchecked(throwable);
      if (throwable instanceof IOException) {
         throw new UncheckedIOException((IOException)throwable);
      } else {
         throw new UndeclaredThrowableException(throwable);
      }
   }

   public static <T extends Throwable> void run(Functions.FailableRunnable<T> runnable) {
      try {
         runnable.run();
      } catch (Throwable t) {
         throw rethrow(t);
      }
   }

   public static <O> Streams.FailableStream<O> stream(Collection<O> collection) {
      return new Streams.FailableStream<>(collection.stream());
   }

   public static <O> Streams.FailableStream<O> stream(Stream<O> stream) {
      return new Streams.FailableStream<>(stream);
   }

   public static <O1, O2, T extends Throwable> boolean test(Functions.FailableBiPredicate<O1, O2, T> predicate, O1 object1, O2 object2) {
      return getAsBoolean(() -> predicate.test(object1, object2));
   }

   public static <O, T extends Throwable> boolean test(Functions.FailablePredicate<O, T> predicate, O object) {
      return getAsBoolean(() -> predicate.test(object));
   }

   @SafeVarargs
   public static void tryWithResources(
      Functions.FailableRunnable<? extends Throwable> action,
      Functions.FailableConsumer<Throwable, ? extends Throwable> errorHandler,
      Functions.FailableRunnable<? extends Throwable>... resources
   ) {
      com.dfsek.terra.lib.commons.lang3.function.FailableRunnable<?>[] fr = new com.dfsek.terra.lib.commons.lang3.function.FailableRunnable[resources.length];
      Arrays.setAll(fr, i -> () -> resources[i].run());
      Failable.tryWithResources(
         action::run,
         errorHandler != null ? errorHandler::accept : null,
         (com.dfsek.terra.lib.commons.lang3.function.FailableRunnable<? extends Throwable>[])fr
      );
   }

   @SafeVarargs
   public static void tryWithResources(Functions.FailableRunnable<? extends Throwable> action, Functions.FailableRunnable<? extends Throwable>... resources) {
      tryWithResources(action, null, resources);
   }

   @Deprecated
   @FunctionalInterface
   public interface FailableBiConsumer<O1, O2, T extends Throwable> {
      void accept(O1 var1, O2 var2) throws T;
   }

   @Deprecated
   @FunctionalInterface
   public interface FailableBiFunction<O1, O2, R, T extends Throwable> {
      R apply(O1 var1, O2 var2) throws T;
   }

   @Deprecated
   @FunctionalInterface
   public interface FailableBiPredicate<O1, O2, T extends Throwable> {
      boolean test(O1 var1, O2 var2) throws T;
   }

   @Deprecated
   @FunctionalInterface
   public interface FailableCallable<R, T extends Throwable> {
      R call() throws T;
   }

   @Deprecated
   @FunctionalInterface
   public interface FailableConsumer<O, T extends Throwable> {
      void accept(O var1) throws T;
   }

   @Deprecated
   @FunctionalInterface
   public interface FailableFunction<I, R, T extends Throwable> {
      R apply(I var1) throws T;
   }

   @Deprecated
   @FunctionalInterface
   public interface FailablePredicate<I, T extends Throwable> {
      boolean test(I var1) throws T;
   }

   @Deprecated
   @FunctionalInterface
   public interface FailableRunnable<T extends Throwable> {
      void run() throws T;
   }

   @Deprecated
   @FunctionalInterface
   public interface FailableSupplier<R, T extends Throwable> {
      R get() throws T;
   }
}
