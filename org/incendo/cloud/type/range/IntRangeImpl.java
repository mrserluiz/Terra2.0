package org.incendo.cloud.type.range;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.immutables.value.Generated;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "IntRange", generator = "Immutables")
@Immutable
final class IntRangeImpl implements IntRange {
   private final int minInt;
   private final int maxInt;

   private IntRangeImpl(int minInt, int maxInt) {
      this.minInt = minInt;
      this.maxInt = maxInt;
   }

   @Override
   public int minInt() {
      return this.minInt;
   }

   @Override
   public int maxInt() {
      return this.maxInt;
   }

   public final IntRangeImpl withMinInt(int value) {
      return this.minInt == value ? this : new IntRangeImpl(value, this.maxInt);
   }

   public final IntRangeImpl withMaxInt(int value) {
      return this.maxInt == value ? this : new IntRangeImpl(this.minInt, value);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof IntRangeImpl && this.equalTo(0, (IntRangeImpl)another);
   }

   private boolean equalTo(int synthetic, IntRangeImpl another) {
      return this.minInt == another.minInt && this.maxInt == another.maxInt;
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.minInt;
      return h + (h << 5) + this.maxInt;
   }

   @Override
   public String toString() {
      return "IntRange{minInt=" + this.minInt + ", maxInt=" + this.maxInt + "}";
   }

   public static IntRangeImpl of(int minInt, int maxInt) {
      return new IntRangeImpl(minInt, maxInt);
   }

   public static IntRangeImpl copyOf(IntRange instance) {
      return instance instanceof IntRangeImpl ? (IntRangeImpl)instance : of(instance.minInt(), instance.maxInt());
   }
}
