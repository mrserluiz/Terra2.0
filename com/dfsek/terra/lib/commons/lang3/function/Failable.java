package com.dfsek.terra.lib.commons.lang3.function;

import com.dfsek.terra.lib.commons.lang3.exception.ExceptionUtils;
import com.dfsek.terra.lib.commons.lang3.stream.Streams;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
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

public class Failable {
   public static <T, U, E extends Throwable> void accept(FailableBiConsumer<T, U, E> consumer, T object1, U object2) {
      run(() -> consumer.accept(object1, object2));
   }

   public static <T, E extends Throwable> void accept(FailableConsumer<T, E> consumer, T object) {
      run(() -> consumer.accept(object));
   }

   public static <E extends Throwable> void accept(FailableDoubleConsumer<E> consumer, double value) {
      run(() -> consumer.accept(value));
   }

   public static <E extends Throwable> void accept(FailableIntConsumer<E> consumer, int value) {
      run(() -> consumer.accept(value));
   }

   public static <E extends Throwable> void accept(FailableLongConsumer<E> consumer, long value) {
      run(() -> consumer.accept(value));
   }

   public static <T, U, R, E extends Throwable> R apply(FailableBiFunction<T, U, R, E> function, T input1, U input2) {
      return get(() -> function.apply(input1, input2));
   }

   public static <T, R, E extends Throwable> R apply(FailableFunction<T, R, E> function, T input) {
      return get(() -> function.apply(input));
   }

   public static <E extends Throwable> double applyAsDouble(FailableDoubleBinaryOperator<E> function, double left, double right) {
      return getAsDouble(() -> function.applyAsDouble(left, right));
   }

   public static <T, U> BiConsumer<T, U> asBiConsumer(FailableBiConsumer<T, U, ?> consumer) {
      return (input1, input2) -> accept(consumer, input1, input2);
   }

   public static <T, U, R> BiFunction<T, U, R> asBiFunction(FailableBiFunction<T, U, R, ?> function) {
      return (input1, input2) -> apply(function, input1, input2);
   }

   public static <T, U> BiPredicate<T, U> asBiPredicate(FailableBiPredicate<T, U, ?> predicate) {
      return (input1, input2) -> test(predicate, input1, input2);
   }

   public static <V> Callable<V> asCallable(FailableCallable<V, ?> callable) {
      return () -> call(callable);
   }

   public static <T> Consumer<T> asConsumer(FailableConsumer<T, ?> consumer) {
      return input -> accept(consumer, input);
   }

   public static <T, R> Function<T, R> asFunction(FailableFunction<T, R, ?> function) {
      return input -> apply(function, input);
   }

   public static <T> Predicate<T> asPredicate(FailablePredicate<T, ?> predicate) {
      return input -> test(predicate, input);
   }

   public static Runnable asRunnable(FailableRunnable<?> runnable) {
      return () -> run(runnable);
   }

   public static <T> Supplier<T> asSupplier(FailableSupplier<T, ?> supplier) {
      return () -> get(supplier);
   }

   public static <V, E extends Throwable> V call(FailableCallable<V, E> callable) {
      return get(callable::call);
   }

   public static <T, E extends Throwable> T get(FailableSupplier<T, E> supplier) {
      try {
         return supplier.get();
      } catch (Throwable t) {
         throw rethrow(t);
      }
   }

   public static <E extends Throwable> boolean getAsBoolean(FailableBooleanSupplier<E> supplier) {
      try {
         return supplier.getAsBoolean();
      } catch (Throwable t) {
         throw rethrow(t);
      }
   }

   public static <E extends Throwable> double getAsDouble(FailableDoubleSupplier<E> supplier) {
      try {
         return supplier.getAsDouble();
      } catch (Throwable t) {
         throw rethrow(t);
      }
   }

   public static <E extends Throwable> int getAsInt(FailableIntSupplier<E> supplier) {
      try {
         return supplier.getAsInt();
      } catch (Throwable t) {
         throw rethrow(t);
      }
   }

   public static <E extends Throwable> long getAsLong(FailableLongSupplier<E> supplier) {
      try {
         return supplier.getAsLong();
      } catch (Throwable t) {
         throw rethrow(t);
      }
   }

   public static <E extends Throwable> short getAsShort(FailableShortSupplier<E> supplier) {
      try {
         return supplier.getAsShort();
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

   public static <E extends Throwable> void run(FailableRunnable<E> runnable) {
      try {
         runnable.run();
      } catch (Throwable t) {
         throw rethrow(t);
      }
   }

   public static <E> Streams.FailableStream<E> stream(Collection<E> collection) {
      return new Streams.FailableStream<>(collection.stream());
   }

   public static <T> Streams.FailableStream<T> stream(Stream<T> stream) {
      return new Streams.FailableStream<>(stream);
   }

   public static <T, U, E extends Throwable> boolean test(FailableBiPredicate<T, U, E> predicate, T object1, U object2) {
      return getAsBoolean(() -> predicate.test(object1, object2));
   }

   public static <T, E extends Throwable> boolean test(FailablePredicate<T, E> predicate, T object) {
      return getAsBoolean(() -> predicate.test(object));
   }

   @SafeVarargs
   public static void tryWithResources(
      FailableRunnable<? extends Throwable> action,
      FailableConsumer<Throwable, ? extends Throwable> errorHandler,
      FailableRunnable<? extends Throwable>... resources
   ) {
      FailableConsumer<Throwable, ? extends Throwable> actualErrorHandler;
      if (errorHandler == null) {
         actualErrorHandler = Failable::rethrow;
      } else {
         actualErrorHandler = errorHandler;
      }

      Streams.of(resources).forEach(r -> Objects.requireNonNull(r, "runnable"));
      Throwable th = null;

      try {
         action.run();
      } catch (Throwable t) {
         th = t;
      }

      if (resources != null) {
         for (FailableRunnable<?> runnable : resources) {
            try {
               runnable.run();
            } catch (Throwable t) {
               if (th == null) {
                  th = t;
               }
            }
         }
      }

      if (th != null) {
         try {
            actualErrorHandler.accept(th);
         } catch (Throwable t) {
            throw rethrow(t);
         }
      }
   }

   @SafeVarargs
   public static void tryWithResources(FailableRunnable<? extends Throwable> action, FailableRunnable<? extends Throwable>... resources) {
      tryWithResources(action, null, resources);
   }

   private Failable() {
   }
}
