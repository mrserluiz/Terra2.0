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
@Generated(from = "ShortRange", generator = "Immutables")
@Immutable
final class ShortRangeImpl implements ShortRange {
   private final short minShort;
   private final short maxShort;

   private ShortRangeImpl(short minShort, short maxShort) {
      this.minShort = minShort;
      this.maxShort = maxShort;
   }

   @Override
   public short minShort() {
      return this.minShort;
   }

   @Override
   public short maxShort() {
      return this.maxShort;
   }

   public final ShortRangeImpl withMinShort(short value) {
      return this.minShort == value ? this : new ShortRangeImpl(value, this.maxShort);
   }

   public final ShortRangeImpl withMaxShort(short value) {
      return this.maxShort == value ? this : new ShortRangeImpl(this.minShort, value);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof ShortRangeImpl && this.equalTo(0, (ShortRangeImpl)another);
   }

   private boolean equalTo(int synthetic, ShortRangeImpl another) {
      return this.minShort == another.minShort && this.maxShort == another.maxShort;
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + Short.hashCode(this.minShort);
      return h + (h << 5) + Short.hashCode(this.maxShort);
   }

   @Override
   public String toString() {
      return "ShortRange{minShort=" + this.minShort + ", maxShort=" + this.maxShort + "}";
   }

   public static ShortRangeImpl of(short minShort, short maxShort) {
      return new ShortRangeImpl(minShort, maxShort);
   }

   public static ShortRangeImpl copyOf(ShortRange instance) {
      return instance instanceof ShortRangeImpl ? (ShortRangeImpl)instance : of(instance.minShort(), instance.maxShort());
   }
}
