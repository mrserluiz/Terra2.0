package com.dfsek.terra.lib.commons.lang3.function;

import com.dfsek.terra.lib.commons.lang3.exception.UncheckedIllegalAccessException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MethodInvokers {
   public static <T, U> BiConsumer<T, U> asBiConsumer(Method method) {
      return asInterfaceInstance(BiConsumer.class, method);
   }

   public static <T, U, R> BiFunction<T, U, R> asBiFunction(Method method) {
      return asInterfaceInstance(BiFunction.class, method);
   }

   public static <T, U> FailableBiConsumer<T, U, Throwable> asFailableBiConsumer(Method method) {
      return asInterfaceInstance(FailableBiConsumer.class, method);
   }

   public static <T, U, R> FailableBiFunction<T, U, R, Throwable> asFailableBiFunction(Method method) {
      return asInterfaceInstance(FailableBiFunction.class, method);
   }

   public static <T, R> FailableFunction<T, R, Throwable> asFailableFunction(Method method) {
      return asInterfaceInstance(FailableFunction.class, method);
   }

   public static <R> FailableSupplier<R, Throwable> asFailableSupplier(Method method) {
      return asInterfaceInstance(FailableSupplier.class, method);
   }

   public static <T, R> Function<T, R> asFunction(Method method) {
      return asInterfaceInstance(Function.class, method);
   }

   public static <T> T asInterfaceInstance(Class<T> interfaceClass, Method method) {
      return MethodHandleProxies.asInterfaceInstance(Objects.requireNonNull(interfaceClass, "interfaceClass"), unreflectUnchecked(method));
   }

   public static <R> Supplier<R> asSupplier(Method method) {
      return asInterfaceInstance(Supplier.class, method);
   }

   private static Method requireMethod(Method method) {
      return Objects.requireNonNull(method, "method");
   }

   private static MethodHandle unreflect(Method method) throws IllegalAccessException {
      return MethodHandles.lookup().unreflect(requireMethod(method));
   }

   private static MethodHandle unreflectUnchecked(Method method) {
      try {
         return unreflect(method);
      } catch (IllegalAccessException e) {
         throw new UncheckedIllegalAccessException(e);
      }
   }

   private MethodInvokers() {
   }
}
