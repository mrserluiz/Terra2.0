package org.incendo.cloud.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public class Quintet<U, V, W, X, Y> implements Tuple {
   private final U first;
   private final V second;
   private final W third;
   private final X fourth;
   private final Y fifth;

   protected Quintet(final @NonNull U first, final @NonNull V second, final @NonNull W third, final @NonNull X fourth, final @NonNull Y fifth) {
      this.first = first;
      this.second = second;
      this.third = third;
      this.fourth = fourth;
      this.fifth = fifth;
   }

   public static <U, V, W, X, Y> @NonNull Quintet<@NonNull U, @NonNull V, @NonNull W, @NonNull X, @NonNull Y> of(
      final @NonNull U first, final @NonNull V second, final @NonNull W third, final @NonNull X fourth, final @NonNull Y fifth
   ) {
      return new Quintet<>(first, second, third, fourth, fifth);
   }

   public final @NonNull U first() {
      return this.first;
   }

   public final @NonNull V second() {
      return this.second;
   }

   public final @NonNull W third() {
      return this.third;
   }

   public final @NonNull X fourth() {
      return this.fourth;
   }

   public final @NonNull Y fifth() {
      return this.fifth;
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Quintet<?, ?, ?, ?, ?> quintet = (Quintet<?, ?, ?, ?, ?>)o;
         return Objects.equals(this.first(), quintet.first())
            && Objects.equals(this.second(), quintet.second())
            && Objects.equals(this.third(), quintet.third())
            && Objects.equals(this.fourth(), quintet.fourth())
            && Objects.equals(this.fifth(), quintet.fifth());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.first(), this.second(), this.third(), this.fourth(), this.fifth());
   }

   @Override
   public final String toString() {
      return String.format("(%s, %s, %s, %s, %s)", this.first, this.second, this.third, this.fourth, this.fifth);
   }

   @Override
   public final int size() {
      return 5;
   }

   @Override
   public final @NonNull Object @NonNull [] toArray() {
      return new Object[]{this.first, this.second, this.third, this.fourth, this.fifth};
   }
}
