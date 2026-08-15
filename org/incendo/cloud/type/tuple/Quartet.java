package org.incendo.cloud.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public class Quartet<U, V, W, X> implements Tuple {
   private final U first;
   private final V second;
   private final W third;
   private final X fourth;

   protected Quartet(final @NonNull U first, final @NonNull V second, final @NonNull W third, final @NonNull X fourth) {
      this.first = first;
      this.second = second;
      this.third = third;
      this.fourth = fourth;
   }

   public static <U, V, W, X> @NonNull Quartet<@NonNull U, @NonNull V, @NonNull W, @NonNull X> of(
      final @NonNull U first, final @NonNull V second, final @NonNull W third, final @NonNull X fourth
   ) {
      return new Quartet<>(first, second, third, fourth);
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

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Quartet<?, ?, ?, ?> quartet = (Quartet<?, ?, ?, ?>)o;
         return Objects.equals(this.first(), quartet.first())
            && Objects.equals(this.second(), quartet.second())
            && Objects.equals(this.third(), quartet.third())
            && Objects.equals(this.fourth(), quartet.fourth());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.first(), this.second(), this.third(), this.fourth());
   }

   @Override
   public final String toString() {
      return String.format("(%s, %s, %s, %s)", this.first, this.second, this.third, this.fourth);
   }

   @Override
   public final int size() {
      return 4;
   }

   @Override
   public final @NonNull Object @NonNull [] toArray() {
      return new Object[]{this.first, this.second, this.third, this.fourth};
   }
}
