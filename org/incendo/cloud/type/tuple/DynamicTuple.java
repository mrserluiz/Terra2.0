package org.incendo.cloud.type.tuple;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public final class DynamicTuple implements Tuple {
   private final Object[] internalArray;

   private DynamicTuple(final @NonNull Object @NonNull [] internalArray) {
      this.internalArray = internalArray;
   }

   public static @NonNull DynamicTuple of(final @NonNull Object... elements) {
      return new DynamicTuple(elements);
   }

   @Override
   public int size() {
      return this.internalArray.length;
   }

   @Override
   public Object[] toArray() {
      Object[] newArray = new Object[this.internalArray.length];
      System.arraycopy(this.internalArray, 0, newArray, 0, this.internalArray.length);
      return newArray;
   }
}
