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
@Generated(from = "FloatRange", generator = "Immutables")
@Immutable
final class FloatRangeImpl implements FloatRange {
   private final float minFloat;
   private final float maxFloat;

   private FloatRangeImpl(float minFloat, float maxFloat) {
      this.minFloat = minFloat;
      this.maxFloat = maxFloat;
   }

   @Override
   public float minFloat() {
      return this.minFloat;
   }

   @Override
   public float maxFloat() {
      return this.maxFloat;
   }

   public final FloatRangeImpl withMinFloat(float value) {
      return Float.floatToIntBits(this.minFloat) == Float.floatToIntBits(value) ? this : new FloatRangeImpl(value, this.maxFloat);
   }

   public final FloatRangeImpl withMaxFloat(float value) {
      return Float.floatToIntBits(this.maxFloat) == Float.floatToIntBits(value) ? this : new FloatRangeImpl(this.minFloat, value);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof FloatRangeImpl && this.equalTo(0, (FloatRangeImpl)another);
   }

   private boolean equalTo(int synthetic, FloatRangeImpl another) {
      return Float.floatToIntBits(this.minFloat) == Float.floatToIntBits(another.minFloat)
         && Float.floatToIntBits(this.maxFloat) == Float.floatToIntBits(another.maxFloat);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + Float.hashCode(this.minFloat);
      return h + (h << 5) + Float.hashCode(this.maxFloat);
   }

   @Override
   public String toString() {
      return "FloatRange{minFloat=" + this.minFloat + ", maxFloat=" + this.maxFloat + "}";
   }

   public static FloatRangeImpl of(float minFloat, float maxFloat) {
      return new FloatRangeImpl(minFloat, maxFloat);
   }

   public static FloatRangeImpl copyOf(FloatRange instance) {
      return instance instanceof FloatRangeImpl ? (FloatRangeImpl)instance : of(instance.minFloat(), instance.maxFloat());
   }
}
