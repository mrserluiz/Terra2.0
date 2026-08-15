package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.DoNotMock;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use Optional.of(value) or Optional.absent()")
@GwtCompatible(serializable = true)
public abstract class Optional<T> implements Serializable {
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <T> Optional<T> absent() {
      return Absent.withType();
   }

   public static <T> Optional<T> of(T reference) {
      return new Present<>(Preconditions.checkNotNull(reference));
   }

   public static <T> Optional<T> fromNullable(@Nullable T nullableReference) {
      return nullableReference == null ? absent() : new Present<>(nullableReference);
   }

   public static <T> @Nullable Optional<T> fromJavaUtil(@Nullable Optional<T> javaUtilOptional) {
      return javaUtilOptional == null ? null : fromNullable(javaUtilOptional.orElse(null));
   }

   public static <T> @Nullable Optional<T> toJavaUtil(@Nullable Optional<T> googleOptional) {
      return googleOptional == null ? null : googleOptional.toJavaUtil();
   }

   public java.util.Optional<T> toJavaUtil() {
      return java.util.Optional.ofNullable(this.orNull());
   }

   Optional() {
   }

   public abstract boolean isPresent();

   public abstract T get();

   public abstract T or(T defaultValue);

   public abstract Optional<T> or(Optional<? extends T> secondChoice);

   public abstract T or(Supplier<? extends T> supplier);

   public abstract @Nullable T orNull();

   public abstract Set<T> asSet();

   public abstract <V> Optional<V> transform(Function<? super T, V> function);

   @Override
   public abstract boolean equals(@Nullable Object object);

   @Override
   public abstract int hashCode();

   @Override
   public abstract String toString();

   public static <T> Iterable<T> presentInstances(Iterable<? extends Optional<? extends T>> optionals) {
      Preconditions.checkNotNull(optionals);
      return () -> new AbstractIterator<T>() {
         private final Iterator<? extends Optional<? extends T>> iterator = Preconditions.checkNotNull(optionals.iterator());

         @Override
         protected @Nullable T computeNext() {
            while (this.iterator.hasNext()) {
               Optional<? extends T> optional = (Optional<? extends T>)this.iterator.next();
               if (optional.isPresent()) {
                  return (T)optional.get();
               }
            }

            return (T)this.endOfData();
         }
      };
   }
}
