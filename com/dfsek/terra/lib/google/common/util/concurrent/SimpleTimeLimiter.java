package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ObjectArrays;
import com.dfsek.terra.lib.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@J2ktIncompatible
@GwtIncompatible
public final class SimpleTimeLimiter implements TimeLimiter {
   private final ExecutorService executor;

   private SimpleTimeLimiter(ExecutorService executor) {
      this.executor = Preconditions.checkNotNull(executor);
   }

   public static SimpleTimeLimiter create(ExecutorService executor) {
      return new SimpleTimeLimiter(executor);
   }

   @Override
   public <T> T newProxy(T target, Class<T> interfaceType, long timeoutDuration, TimeUnit timeoutUnit) {
      Preconditions.checkNotNull(target);
      Preconditions.checkNotNull(interfaceType);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Preconditions.checkArgument(interfaceType.isInterface(), "interfaceType must be an interface type");
      Set<Method> interruptibleMethods = findInterruptibleMethods(interfaceType);
      InvocationHandler handler = (obj, method, args) -> {
         Callable<Object> callable = () -> {
            try {
               return method.invoke(target, args);
            } catch (InvocationTargetException e) {
               throw throwCause(e, false);
            }
         };
         return this.callWithTimeout(callable, timeoutDuration, timeoutUnit, interruptibleMethods.contains(method));
      };
      return newProxy(interfaceType, handler);
   }

   private static <T> T newProxy(Class<T> interfaceType, InvocationHandler handler) {
      Object object = Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, handler);
      return interfaceType.cast(object);
   }

   @ParametricNullness
   private <T> T callWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit, boolean amInterruptible) throws Exception {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<T> future = this.executor.submit(callable);

      try {
         return amInterruptible ? future.get(timeoutDuration, timeoutUnit) : Uninterruptibles.getUninterruptibly(future, timeoutDuration, timeoutUnit);
      } catch (InterruptedException e) {
         future.cancel(true);
         throw e;
      } catch (ExecutionException e) {
         throw throwCause(e, true);
      } catch (TimeoutException e) {
         future.cancel(true);
         throw new UncheckedTimeoutException(e);
      }
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public <T> T callWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, InterruptedException, ExecutionException {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<T> future = this.executor.submit(callable);

      try {
         return future.get(timeoutDuration, timeoutUnit);
      } catch (InterruptedException | TimeoutException e) {
         future.cancel(true);
         throw e;
      } catch (ExecutionException e) {
         this.wrapAndThrowExecutionExceptionOrError(e.getCause());
         throw new AssertionError();
      }
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public <T> T callUninterruptiblyWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, ExecutionException {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<T> future = this.executor.submit(callable);

      try {
         return Uninterruptibles.getUninterruptibly(future, timeoutDuration, timeoutUnit);
      } catch (TimeoutException e) {
         future.cancel(true);
         throw e;
      } catch (ExecutionException e) {
         this.wrapAndThrowExecutionExceptionOrError(e.getCause());
         throw new AssertionError();
      }
   }

   @Override
   public void runWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, InterruptedException {
      Preconditions.checkNotNull(runnable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<?> future = this.executor.submit(runnable);

      try {
         future.get(timeoutDuration, timeoutUnit);
      } catch (InterruptedException | TimeoutException e) {
         future.cancel(true);
         throw e;
      } catch (ExecutionException e) {
         this.wrapAndThrowRuntimeExecutionExceptionOrError(e.getCause());
         throw new AssertionError();
      }
   }

   @Override
   public void runUninterruptiblyWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException {
      Preconditions.checkNotNull(runnable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<?> future = this.executor.submit(runnable);

      try {
         Uninterruptibles.getUninterruptibly(future, timeoutDuration, timeoutUnit);
      } catch (TimeoutException e) {
         future.cancel(true);
         throw e;
      } catch (ExecutionException e) {
         this.wrapAndThrowRuntimeExecutionExceptionOrError(e.getCause());
         throw new AssertionError();
      }
   }

   private static Exception throwCause(Exception e, boolean combineStackTraces) throws Exception {
      Throwable cause = e.getCause();
      if (cause == null) {
         throw e;
      }

      if (combineStackTraces) {
         StackTraceElement[] combined = ObjectArrays.concat(cause.getStackTrace(), e.getStackTrace(), StackTraceElement.class);
         cause.setStackTrace(combined);
      }

      if (cause instanceof Exception) {
         throw (Exception)cause;
      } else if (cause instanceof Error) {
         throw (Error)cause;
      } else {
         throw e;
      }
   }

   private static Set<Method> findInterruptibleMethods(Class<?> interfaceType) {
      Set<Method> set = Sets.newHashSet();

      for (Method m : interfaceType.getMethods()) {
         if (declaresInterruptedEx(m)) {
            set.add(m);
         }
      }

      return set;
   }

   private static boolean declaresInterruptedEx(Method method) {
      for (Class<?> exType : method.getExceptionTypes()) {
         if (exType == InterruptedException.class) {
            return true;
         }
      }

      return false;
   }

   private void wrapAndThrowExecutionExceptionOrError(Throwable cause) throws ExecutionException {
      if (cause instanceof Error) {
         throw new ExecutionError((Error)cause);
      } else if (cause instanceof RuntimeException) {
         throw new UncheckedExecutionException(cause);
      } else {
         throw new ExecutionException(cause);
      }
   }

   private void wrapAndThrowRuntimeExecutionExceptionOrError(Throwable cause) {
      if (cause instanceof Error) {
         throw new ExecutionError((Error)cause);
      } else {
         throw new UncheckedExecutionException(cause);
      }
   }

   private static void checkPositiveTimeout(long timeoutDuration) {
      Preconditions.checkArgument(timeoutDuration > 0L, "timeout must be positive: %s", timeoutDuration);
   }
}
