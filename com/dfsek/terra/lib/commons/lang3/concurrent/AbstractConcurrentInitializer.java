package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.builder.AbstractSupplier;
import com.dfsek.terra.lib.commons.lang3.exception.ExceptionUtils;
import com.dfsek.terra.lib.commons.lang3.function.FailableConsumer;
import com.dfsek.terra.lib.commons.lang3.function.FailableSupplier;
import java.util.Objects;

public abstract class AbstractConcurrentInitializer<T, E extends Exception> implements ConcurrentInitializer<T> {
   private final FailableConsumer<? super T, ? extends Exception> closer;
   private final FailableSupplier<? extends T, ? extends Exception> initializer;

   public AbstractConcurrentInitializer() {
      this(FailableSupplier.nul(), FailableConsumer.nop());
   }

   AbstractConcurrentInitializer(FailableSupplier<? extends T, ? extends Exception> initializer, FailableConsumer<? super T, ? extends Exception> closer) {
      this.closer = Objects.requireNonNull(closer, "closer");
      this.initializer = Objects.requireNonNull(initializer, "initializer");
   }

   public void close() throws ConcurrentException {
      if (this.isInitialized()) {
         try {
            this.closer.accept(this.get());
         } catch (Exception e) {
            throw new ConcurrentException(ExceptionUtils.throwUnchecked(e));
         }
      }
   }

   protected abstract E getTypedException(Exception var1);

   protected T initialize() throws E {
      try {
         return (T)this.initializer.get();
      } catch (Exception e) {
         ExceptionUtils.throwUnchecked(e);
         E typedException = this.getTypedException(e);
         if (typedException.getClass().isAssignableFrom(e.getClass())) {
            throw e;
         } else {
            throw typedException;
         }
      }
   }

   protected abstract boolean isInitialized();

   public abstract static class AbstractBuilder<I extends AbstractConcurrentInitializer<T, E>, T, B extends AbstractConcurrentInitializer.AbstractBuilder<I, T, B, E>, E extends Exception>
      extends AbstractSupplier<I, B, E> {
      private FailableConsumer<T, ? extends Exception> closer = FailableConsumer.nop();
      private FailableSupplier<T, ? extends Exception> initializer = FailableSupplier.nul();

      public FailableConsumer<T, ? extends Exception> getCloser() {
         return this.closer;
      }

      public FailableSupplier<T, ? extends Exception> getInitializer() {
         return this.initializer;
      }

      public B setCloser(FailableConsumer<T, ? extends Exception> closer) {
         this.closer = closer != null ? closer : FailableConsumer.nop();
         return this.asThis();
      }

      public B setInitializer(FailableSupplier<T, ? extends Exception> initializer) {
         this.initializer = initializer != null ? initializer : FailableSupplier.nul();
         return this.asThis();
      }
   }
}
