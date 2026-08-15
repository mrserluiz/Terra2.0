package com.dfsek.terra.lib.commons.lang3.function;

import java.util.Objects;

public interface FailableLongUnaryOperator<E extends Throwable> {
   FailableLongUnaryOperator NOP = t -> 0L;

   static <E extends Throwable> FailableLongUnaryOperator<E> identity() {
      return t -> t;
   }

   static <E extends Throwable> FailableLongUnaryOperator<E> nop() {
      return NOP;
   }

   default FailableLongUnaryOperator<E> andThen(FailableLongUnaryOperator<E> after) {
      Objects.requireNonNull(after);
      return t -> after.applyAsLong(this.applyAsLong(t));
   }

   long applyAsLong(long var1) throws E;

   default FailableLongUnaryOperator<E> compose(FailableLongUnaryOperator<E> before) {
      Objects.requireNonNull(before);
      return v -> this.applyAsLong(before.applyAsLong(v));
   }
}
