package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Supplier;
import com.dfsek.terra.lib.google.common.collect.ForwardingSet;
import java.util.Set;

final class InvalidatableSet<E> extends ForwardingSet<E> {
   private final Supplier<Boolean> validator;
   private final Set<E> delegate;
   private final Supplier<String> errorMessage;

   static <E> InvalidatableSet<E> of(Set<E> delegate, Supplier<Boolean> validator, Supplier<String> errorMessage) {
      return new InvalidatableSet<>(Preconditions.checkNotNull(delegate), Preconditions.checkNotNull(validator), Preconditions.checkNotNull(errorMessage));
   }

   @Override
   protected Set<E> delegate() {
      this.validate();
      return this.delegate;
   }

   private InvalidatableSet(Set<E> delegate, Supplier<Boolean> validator, Supplier<String> errorMessage) {
      this.delegate = delegate;
      this.validator = validator;
      this.errorMessage = errorMessage;
   }

   @Override
   public int hashCode() {
      return this.delegate.hashCode();
   }

   private void validate() {
      if (!this.validator.get()) {
         throw new IllegalStateException(this.errorMessage.get());
      }
   }
}
