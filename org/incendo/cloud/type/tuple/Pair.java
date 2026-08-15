package org.incendo.cloud.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public class Pair<U, V> implements Tuple {
   private final U first;
   private final V second;

   protected Pair(final U first, final V second) {
      this.first = first;
      this.second = second;
   }

   public static <U, V> @NonNull Pair<U, V> of(final U first, final V second) {
      return new Pair<>(first, second);
   }

   public final U first() {
      return this.first;
   }

   public final V second() {
      return this.second;
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Pair<?, ?> pair = (Pair<?, ?>)o;
         return Objects.equals(this.first(), pair.first()) && Objects.equals(this.second(), pair.second());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.first(), this.second());
   }

   @Override
   public final String toString() {
      return String.format("(%s, %s)", this.first, this.second);
   }

   @Override
   public final int size() {
      return 2;
   }

   @Override
   public final @NonNull Object @NonNull [] toArray() {
      return new Object[]{this.first, this.second};
   }
}
