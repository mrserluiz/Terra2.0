package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class Suppliers {
   private Suppliers() {
   }

   public static <F, T> Supplier<T> compose(Function<? super F, T> function, Supplier<F> supplier) {
      return new Suppliers.SupplierComposition<>(function, supplier);
   }

   public static <T> Supplier<T> memoize(Supplier<T> delegate) {
      if (!(delegate instanceof Suppliers.NonSerializableMemoizingSupplier) && !(delegate instanceof Suppliers.MemoizingSupplier)) {
         return delegate instanceof Serializable ? new Suppliers.MemoizingSupplier<>(delegate) : new Suppliers.NonSerializableMemoizingSupplier<>(delegate);
      } else {
         return delegate;
      }
   }

   public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> delegate, long duration, TimeUnit unit) {
      Preconditions.checkNotNull(delegate);
      Preconditions.checkArgument(duration > 0L, "duration (%s %s) must be > 0", duration, unit);
      return new Suppliers.ExpiringMemoizingSupplier<>(delegate, unit.toNanos(duration));
   }

   @J2ktIncompatible
   @GwtIncompatible
   @IgnoreJRERequirement
   public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> delegate, Duration duration) {
      Preconditions.checkNotNull(delegate);
      Preconditions.checkArgument(!duration.isNegative() && !duration.isZero(), "duration (%s) must be > 0", duration);
      return new Suppliers.ExpiringMemoizingSupplier<>(delegate, Internal.toNanosSaturated(duration));
   }

   public static <T> Supplier<T> ofInstance(@ParametricNullness T instance) {
      return new Suppliers.SupplierOfInstance<>(instance);
   }

   @J2ktIncompatible
   public static <T> Supplier<T> synchronizedSupplier(Supplier<T> delegate) {
      return new Suppliers.ThreadSafeSupplier<>(delegate);
   }

   public static <T> Function<Supplier<T>, T> supplierFunction() {
      return Suppliers.SupplierFunctionImpl.INSTANCE;
   }

   @VisibleForTesting
   static class ExpiringMemoizingSupplier<T> implements Supplier<T>, Serializable {
      private transient Object lock = new Object();
      final Supplier<T> delegate;
      final long durationNanos;
      transient volatile @Nullable T value;
      transient volatile long expirationNanos;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      ExpiringMemoizingSupplier(Supplier<T> delegate, long durationNanos) {
         this.delegate = delegate;
         this.durationNanos = durationNanos;
      }

      @ParametricNullness
      @Override
      public T get() {
         long nanos = this.expirationNanos;
         long now = System.nanoTime();
         if (nanos == 0L || now - nanos >= 0L) {
            synchronized (this.lock) {
               if (nanos == this.expirationNanos) {
                  T t = this.delegate.get();
                  this.value = t;
                  nanos = now + this.durationNanos;
                  this.expirationNanos = nanos == 0L ? 1L : nanos;
                  return t;
               }
            }
         }

         return NullnessCasts.uncheckedCastNullableTToT(this.value);
      }

      @Override
      public String toString() {
         return "Suppliers.memoizeWithExpiration(" + this.delegate + ", " + this.durationNanos + ", NANOS)";
      }

      @GwtIncompatible
      @J2ktIncompatible
      private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         this.lock = new Object();
      }
   }

   @VisibleForTesting
   static class MemoizingSupplier<T> implements Supplier<T>, Serializable {
      private transient Object lock = new Object();
      final Supplier<T> delegate;
      transient volatile boolean initialized;
      transient @Nullable T value;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      MemoizingSupplier(Supplier<T> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @ParametricNullness
      @Override
      public T get() {
         if (!this.initialized) {
            synchronized (this.lock) {
               if (!this.initialized) {
                  T t = this.delegate.get();
                  this.value = t;
                  this.initialized = true;
                  return t;
               }
            }
         }

         return NullnessCasts.uncheckedCastNullableTToT(this.value);
      }

      @Override
      public String toString() {
         return "Suppliers.memoize(" + (this.initialized ? "<supplier that returned " + this.value + ">" : this.delegate) + ")";
      }

      @GwtIncompatible
      @J2ktIncompatible
      private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         this.lock = new Object();
      }
   }

   @VisibleForTesting
   static class NonSerializableMemoizingSupplier<T> implements Supplier<T> {
      private final Object lock = new Object();
      private static final Supplier<@Nullable Void> SUCCESSFULLY_COMPUTED = () -> {
         throw new IllegalStateException();
      };
      private volatile Supplier<T> delegate;
      private @Nullable T value;

      NonSerializableMemoizingSupplier(Supplier<T> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @ParametricNullness
      @Override
      public T get() {
         if (this.delegate != SUCCESSFULLY_COMPUTED) {
            synchronized (this.lock) {
               if (this.delegate != SUCCESSFULLY_COMPUTED) {
                  T t = this.delegate.get();
                  this.value = t;
                  this.delegate = (Supplier<T>)SUCCESSFULLY_COMPUTED;
                  return t;
               }
            }
         }

         return NullnessCasts.uncheckedCastNullableTToT(this.value);
      }

      @Override
      public String toString() {
         Supplier<T> delegate = this.delegate;
         return "Suppliers.memoize(" + (delegate == SUCCESSFULLY_COMPUTED ? "<supplier that returned " + this.value + ">" : delegate) + ")";
      }
   }

   private static class SupplierComposition<F, T> implements Supplier<T>, Serializable {
      final Function<? super F, T> function;
      final Supplier<F> supplier;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      SupplierComposition(Function<? super F, T> function, Supplier<F> supplier) {
         this.function = Preconditions.checkNotNull(function);
         this.supplier = Preconditions.checkNotNull(supplier);
      }

      @ParametricNullness
      @Override
      public T get() {
         return this.function.apply(this.supplier.get());
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (!(obj instanceof Suppliers.SupplierComposition)) {
            return false;
         }

         Suppliers.SupplierComposition<?, ?> that = (Suppliers.SupplierComposition<?, ?>)obj;
         return this.function.equals(that.function) && this.supplier.equals(that.supplier);
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.function, this.supplier);
      }

      @Override
      public String toString() {
         return "Suppliers.compose(" + this.function + ", " + this.supplier + ")";
      }
   }

   private interface SupplierFunction<T> extends Function<Supplier<T>, T> {
   }

   private enum SupplierFunctionImpl implements Suppliers.SupplierFunction<Object> {
      INSTANCE;

      public @Nullable Object apply(Supplier<@Nullable Object> input) {
         return input.get();
      }

      @Override
      public String toString() {
         return "Suppliers.supplierFunction()";
      }
   }

   private static class SupplierOfInstance<T> implements Supplier<T>, Serializable {
      @ParametricNullness
      final T instance;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      SupplierOfInstance(@ParametricNullness T instance) {
         this.instance = instance;
      }

      @ParametricNullness
      @Override
      public T get() {
         return this.instance;
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (obj instanceof Suppliers.SupplierOfInstance) {
            Suppliers.SupplierOfInstance<?> that = (Suppliers.SupplierOfInstance<?>)obj;
            return Objects.equal(this.instance, that.instance);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.instance);
      }

      @Override
      public String toString() {
         return "Suppliers.ofInstance(" + this.instance + ")";
      }
   }

   @J2ktIncompatible
   private static class ThreadSafeSupplier<T> implements Supplier<T>, Serializable {
      final Supplier<T> delegate;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      ThreadSafeSupplier(Supplier<T> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @ParametricNullness
      @Override
      public T get() {
         synchronized (this.delegate) {
            return this.delegate.get();
         }
      }

      @Override
      public String toString() {
         return "Suppliers.synchronizedSupplier(" + this.delegate + ")";
      }
   }
}
