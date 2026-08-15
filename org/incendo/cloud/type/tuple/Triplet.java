package org.incendo.cloud.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public class Triplet<U, V, W> implements Tuple {
   private final U first;
   private final V second;
   private final W third;

   protected Triplet(final @NonNull U first, final @NonNull V second, final @NonNull W third) {
      this.first = first;
      this.second = second;
      this.third = third;
   }

   public static <U, V, W> @NonNull Triplet<@NonNull U, @NonNull V, @NonNull W> of(final @NonNull U first, final @NonNull V second, final @NonNull W third) {
      return new Triplet<>(first, second, third);
   }

   public final U first() {
      return this.first;
   }

   public final V second() {
      return this.second;
   }

   public final W third() {
      return this.third;
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>)o;
         return Objects.equals(this.first(), triplet.first())
            && Objects.equals(this.second(), triplet.second())
            && Objects.equals(this.third(), triplet.third());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.first(), this.second(), this.third());
   }

   @Override
   public final String toString() {
      return String.format("(%s, %s, %s)", this.first, this.second, this.third);
   }

   @Override
   public final int size() {
      return 3;
   }

   @Override
   public final @NonNull Object @NonNull [] toArray() {
      return new Object[]{this.first, this.second, this.third};
   }
}
