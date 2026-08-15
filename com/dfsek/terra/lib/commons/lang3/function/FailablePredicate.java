package com.dfsek.terra.lib.commons.lang3.function;

import java.util.Objects;

@FunctionalInterface
public interface FailablePredicate<T, E extends Throwable> {
   FailablePredicate FALSE = t -> false;
   FailablePredicate TRUE = t -> true;

   static <T, E extends Throwable> FailablePredicate<T, E> falsePredicate() {
      return FALSE;
   }

   static <T, E extends Throwable> FailablePredicate<T, E> truePredicate() {
      return TRUE;
   }

   default FailablePredicate<T, E> and(FailablePredicate<? super T, E> other) {
      Objects.requireNonNull(other);
      return t -> this.test(t) && other.test(t);
   }

   default FailablePredicate<T, E> negate() {
      return t -> !this.test(t);
   }

   default FailablePredicate<T, E> or(FailablePredicate<? super T, E> other) {
      Objects.requireNonNull(other);
      return t -> this.test(t) || other.test(t);
   }

   boolean test(T var1) throws E;
}
