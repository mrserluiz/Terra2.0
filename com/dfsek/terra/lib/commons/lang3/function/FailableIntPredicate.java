package com.dfsek.terra.lib.commons.lang3.function;

import java.util.Objects;

@FunctionalInterface
public interface FailableIntPredicate<E extends Throwable> {
   FailableIntPredicate FALSE = t -> false;
   FailableIntPredicate TRUE = t -> true;

   static <E extends Throwable> FailableIntPredicate<E> falsePredicate() {
      return FALSE;
   }

   static <E extends Throwable> FailableIntPredicate<E> truePredicate() {
      return TRUE;
   }

   default FailableIntPredicate<E> and(FailableIntPredicate<E> other) {
      Objects.requireNonNull(other);
      return t -> this.test(t) && other.test(t);
   }

   default FailableIntPredicate<E> negate() {
      return t -> !this.test(t);
   }

   default FailableIntPredicate<E> or(FailableIntPredicate<E> other) {
      Objects.requireNonNull(other);
      return t -> this.test(t) || other.test(t);
   }

   boolean test(int var1) throws E;
}
