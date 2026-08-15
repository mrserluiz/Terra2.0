package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.function.FailableConsumer;
import com.dfsek.terra.lib.commons.lang3.function.FailableSupplier;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicSafeInitializer<T> extends AbstractConcurrentInitializer<T, ConcurrentException> {
   private static final Object NO_INIT = new Object();
   private final AtomicReference<AtomicSafeInitializer<T>> factory = new AtomicReference<>();
   private final AtomicReference<T> reference = new AtomicReference<>(this.getNoInit());

   public static <T> AtomicSafeInitializer.Builder<AtomicSafeInitializer<T>, T> builder() {
      return new AtomicSafeInitializer.Builder<>();
   }

   public AtomicSafeInitializer() {
   }

   private AtomicSafeInitializer(FailableSupplier<T, ConcurrentException> initializer, FailableConsumer<T, ConcurrentException> closer) {
      super(initializer, closer);
   }

   @Override
   public final T get() throws ConcurrentException {
      T result;
      while ((result = this.reference.get()) == this.getNoInit()) {
         if (this.factory.compareAndSet(null, this)) {
            this.reference.set(this.initialize());
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

   public static class Builder<I extends AtomicSafeInitializer<T>, T>
      extends AbstractConcurrentInitializer.AbstractBuilder<I, T, AtomicSafeInitializer.Builder<I, T>, ConcurrentException> {
      public I get() {
         return (I)(new AtomicSafeInitializer(this.getInitializer(), this.getCloser()));
      }
   }
}
