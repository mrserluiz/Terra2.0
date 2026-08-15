package com.dfsek.terra.lib.commons.lang3.function;

import java.util.Objects;

public interface FailableDoubleUnaryOperator<E extends Throwable> {
   FailableDoubleUnaryOperator NOP = t -> 0.0;

   static <E extends Throwable> FailableDoubleUnaryOperator<E> identity() {
      return t -> t;
   }

   static <E extends Throwable> FailableDoubleUnaryOperator<E> nop() {
      return NOP;
   }

   default FailableDoubleUnaryOperator<E> andThen(FailableDoubleUnaryOperator<E> after) {
      Objects.requireNonNull(after);
      return t -> after.applyAsDouble(this.applyAsDouble(t));
   }

   double applyAsDouble(double var1) throws E;

   default FailableDoubleUnaryOperator<E> compose(FailableDoubleUnaryOperator<E> before) {
      Objects.requireNonNull(before);
      return v -> this.applyAsDouble(before.applyAsDouble(v));
   }
}
