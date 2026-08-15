package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.Validate;
import com.dfsek.terra.lib.commons.lang3.exception.ExceptionUtils;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConcurrentUtils {
   static Throwable checkedException(Throwable ex) {
      Validate.isTrue(ExceptionUtils.isChecked(ex), "Not a checked exception: " + ex);
      return ex;
   }

   public static <T> Future<T> constantFuture(T value) {
      return new ConcurrentUtils.ConstantFuture<>(value);
   }

   public static <K, V> V createIfAbsent(ConcurrentMap<K, V> map, K key, ConcurrentInitializer<V> init) throws ConcurrentException {
      if (map != null && init != null) {
         V value = map.get(key);
         return value == null ? putIfAbsent(map, key, init.get()) : value;
      } else {
         return null;
      }
   }

   public static <K, V> V createIfAbsentUnchecked(ConcurrentMap<K, V> map, K key, ConcurrentInitializer<V> init) {
      try {
         return createIfAbsent(map, key, init);
      } catch (ConcurrentException cex) {
         throw new ConcurrentRuntimeException(cex.getCause());
      }
   }

   public static ConcurrentException extractCause(ExecutionException ex) {
      if (ex != null && ex.getCause() != null) {
         ExceptionUtils.throwUnchecked(ex.getCause());
         return new ConcurrentException(ex.getMessage(), ex.getCause());
      } else {
         return null;
      }
   }

   public static ConcurrentRuntimeException extractCauseUnchecked(ExecutionException ex) {
      if (ex != null && ex.getCause() != null) {
         ExceptionUtils.throwUnchecked(ex.getCause());
         return new ConcurrentRuntimeException(ex.getMessage(), ex.getCause());
      } else {
         return null;
      }
   }

   public static void handleCause(ExecutionException ex) throws ConcurrentException {
      ConcurrentException cause = extractCause(ex);
      if (cause != null) {
         throw cause;
      }
   }

   public static void handleCauseUnchecked(ExecutionException ex) {
      ConcurrentRuntimeException cause = extractCauseUnchecked(ex);
      if (cause != null) {
         throw cause;
      }
   }

   public static <T> T initialize(ConcurrentInitializer<T> initializer) throws ConcurrentException {
      return initializer != null ? initializer.get() : null;
   }

   public static <T> T initializeUnchecked(ConcurrentInitializer<T> initializer) {
      try {
         return initialize(initializer);
      } catch (ConcurrentException cex) {
         throw new ConcurrentRuntimeException(cex.getCause());
      }
   }

   public static <K, V> V putIfAbsent(ConcurrentMap<K, V> map, K key, V value) {
      if (map == null) {
         return null;
      }

      V result = map.putIfAbsent(key, value);
      return result != null ? result : value;
   }

   private ConcurrentUtils() {
   }

   static final class ConstantFuture<T> implements Future<T> {
      private final T value;

      ConstantFuture(T value) {
         this.value = value;
      }

      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
         return false;
      }

      @Override
      public T get() {
         return this.value;
      }

      @Override
      public T get(long timeout, TimeUnit unit) {
         return this.value;
      }

      @Override
      public boolean isCancelled() {
         return false;
      }

      @Override
      public boolean isDone() {
         return true;
      }
   }
}
