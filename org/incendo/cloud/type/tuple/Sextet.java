package org.incendo.cloud.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public class Sextet<U, V, W, X, Y, Z> implements Tuple {
   private final U first;
   private final V second;
   private final W third;
   private final X fourth;
   private final Y fifth;
   private final Z sixth;

   protected Sextet(
      final @NonNull U first, final @NonNull V second, final @NonNull W third, final @NonNull X fourth, final @NonNull Y fifth, final @NonNull Z sixth
   ) {
      this.first = first;
      this.second = second;
      this.third = third;
      this.fourth = fourth;
      this.fifth = fifth;
      this.sixth = sixth;
   }

   public static <U, V, W, X, Y, Z> @NonNull Sextet<@NonNull U, @NonNull V, @NonNull W, @NonNull X, @NonNull Y, @NonNull Z> of(
      final @NonNull U first, final @NonNull V second, final @NonNull W third, final @NonNull X fourth, final @NonNull Y fifth, final @NonNull Z sixth
   ) {
      return new Sextet<>(first, second, third, fourth, fifth, sixth);
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

   public final @NonNull Z sixth() {
      return this.sixth;
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Sextet<?, ?, ?, ?, ?, ?> sextet = (Sextet<?, ?, ?, ?, ?, ?>)o;
         return Objects.equals(this.first(), sextet.first())
            && Objects.equals(this.second(), sextet.second())
            && Objects.equals(this.third(), sextet.third())
            && Objects.equals(this.fourth(), sextet.fourth())
            && Objects.equals(this.fifth(), sextet.fifth())
            && Objects.equals(this.sixth(), sextet.sixth());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.first(), this.second(), this.third(), this.fourth(), this.fifth(), this.sixth());
   }

   @Override
   public final String toString() {
      return String.format("(%s, %s, %s, %s, %s, %s)", this.first, this.second, this.third, this.fourth, this.fifth, this.sixth);
   }

   @Override
   public final int size() {
      return 6;
   }

   @Override
   public final @NonNull Object @NonNull [] toArray() {
      return new Object[]{this.first, this.second, this.third, this.fourth, this.fifth, this.sixth};
   }
}
