package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface IOFunction<T, R> {
   static <T> IOFunction<T, T> identity() {
      return Constants.IO_FUNCTION_ID;
   }

   default IOConsumer<T> andThen(Consumer<? super R> after) {
      Objects.requireNonNull(after, "after");
      return t -> after.accept(this.apply(t));
   }

   default <V> IOFunction<T, V> andThen(Function<? super R, ? extends V> after) {
      Objects.requireNonNull(after, "after");
      return t -> (V)after.apply(this.apply(t));
   }

   default IOConsumer<T> andThen(IOConsumer<? super R> after) {
      Objects.requireNonNull(after, "after");
      return t -> after.accept(this.apply(t));
   }

   default <V> IOFunction<T, V> andThen(IOFunction<? super R, ? extends V> after) {
      Objects.requireNonNull(after, "after");
      return t -> (V)after.apply(this.apply(t));
   }

   R apply(T var1) throws IOException;

   default Function<T, R> asFunction() {
      return t -> Uncheck.apply(this, t);
   }

   default <V> IOFunction<V, R> compose(Function<? super V, ? extends T> before) {
      Objects.requireNonNull(before, "before");
      return v -> this.apply((T)before.apply(v));
   }

   default <V> IOFunction<V, R> compose(IOFunction<? super V, ? extends T> before) {
      Objects.requireNonNull(before, "before");
      return v -> this.apply((T)before.apply(v));
   }

   default IOSupplier<R> compose(IOSupplier<? extends T> before) {
      Objects.requireNonNull(before, "before");
      return () -> this.apply((T)before.get());
   }

   default IOSupplier<R> compose(Supplier<? extends T> before) {
      Objects.requireNonNull(before, "before");
      return () -> this.apply((T)before.get());
   }
}
