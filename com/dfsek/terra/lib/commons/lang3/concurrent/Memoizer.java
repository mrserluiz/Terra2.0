package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.exception.ExceptionUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Memoizer<I, O> implements Computable<I, O> {
   private final ConcurrentMap<I, Future<O>> cache = new ConcurrentHashMap<>();
   private final Function<? super I, ? extends Future<O>> mappingFunction;
   private final boolean recalculate;

   public Memoizer(Computable<I, O> computable) {
      this(computable, false);
   }

   public Memoizer(Computable<I, O> computable, boolean recalculate) {
      this.recalculate = recalculate;
      this.mappingFunction = k -> FutureTasks.run(() -> computable.compute((I)k));
   }

   public Memoizer(Function<I, O> function) {
      this(function, false);
   }

   public Memoizer(Function<I, O> function, boolean recalculate) {
      this.recalculate = recalculate;
      this.mappingFunction = k -> FutureTasks.run(() -> function.apply((I)k));
   }

   @Override
   public O compute(I arg) throws InterruptedException {
      while (true) {
         Future<O> future = this.cache.computeIfAbsent(arg, this.mappingFunction);

         try {
            return future.get();
         } catch (CancellationException e) {
            this.cache.remove(arg, future);
         } catch (ExecutionException e) {
            if (this.recalculate) {
               this.cache.remove(arg, future);
            }

            throw this.launderException(e.getCause());
         }
      }
   }

   private RuntimeException launderException(Throwable throwable) {
      throw new IllegalStateException("Unchecked exception", ExceptionUtils.throwUnchecked(throwable));
   }
}
