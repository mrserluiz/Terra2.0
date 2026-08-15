package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.function.FailableConsumer;
import com.dfsek.terra.lib.commons.lang3.function.FailableSupplier;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicInitializer<T> extends AbstractConcurrentInitializer<T, ConcurrentException> {
   private static final Object NO_INIT = new Object();
   private final AtomicReference<T> reference = new AtomicReference<>(this.getNoInit());

   public static <T> AtomicInitializer.Builder<AtomicInitializer<T>, T> builder() {
      return new AtomicInitializer.Builder<>();
   }

   public AtomicInitializer() {
   }

   private AtomicInitializer(FailableSupplier<T, ConcurrentException> initializer, FailableConsumer<T, ConcurrentException> closer) {
      super(initializer, closer);
   }

   @Override
   public T get() throws ConcurrentException {
      T result = this.reference.get();
      if (result == this.getNoInit()) {
         result = this.initialize();
         if (!this.reference.compareAndSet(this.getNoInit(), result)) {
            result = this.reference.get();
         }
      }

      return result;
   }

   private T getNoInit() {
      return (T)NO_INIT;
   }

   protected ConcurrentException getTypedException(Exception e) {
      return new ConcurrentException(e);
   }

   @Override
   public boolean isInitialized() {
      return this.reference.get() != NO_INIT;
   }

   public static class Builder<I extends AtomicInitializer<T>, T>
      extends AbstractConcurrentInitializer.AbstractBuilder<I, T, AtomicInitializer.Builder<I, T>, ConcurrentException> {
      public I get() {
         return (I)(new AtomicInitializer(this.getInitializer(), this.getCloser()));
      }
   }
}
