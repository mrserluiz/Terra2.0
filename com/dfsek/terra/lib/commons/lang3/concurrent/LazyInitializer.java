package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.function.FailableConsumer;
import com.dfsek.terra.lib.commons.lang3.function.FailableSupplier;

public class LazyInitializer<T> extends AbstractConcurrentInitializer<T, ConcurrentException> {
   private static final Object NO_INIT = new Object();
   private volatile T object = (T)NO_INIT;

   public static <T> LazyInitializer.Builder<LazyInitializer<T>, T> builder() {
      return new LazyInitializer.Builder<>();
   }

   public LazyInitializer() {
   }

   private LazyInitializer(FailableSupplier<T, ConcurrentException> initializer, FailableConsumer<T, ConcurrentException> closer) {
      super(initializer, closer);
   }

   @Override
   public T get() throws ConcurrentException {
      T result = this.object;
      if (result == NO_INIT) {
         synchronized (this) {
            result = this.object;
            if (result == NO_INIT) {
               this.object = result = this.initialize();
            }
         }
      }

      return result;
   }

   protected ConcurrentException getTypedException(Exception e) {
      return new ConcurrentException(e);
   }

   @Override
   public boolean isInitialized() {
      return this.object != NO_INIT;
   }

   public static class Builder<I extends LazyInitializer<T>, T>
      extends AbstractConcurrentInitializer.AbstractBuilder<I, T, LazyInitializer.Builder<I, T>, ConcurrentException> {
      public I get() {
         return (I)(new LazyInitializer(this.getInitializer(), this.getCloser()));
      }
   }
}
