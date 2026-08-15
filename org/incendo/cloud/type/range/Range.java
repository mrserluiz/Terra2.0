package org.incendo.cloud.type.range;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface Range<N extends Number> {
   @NonNull N min();

   @NonNull N max();

   static @NonNull ByteRange byteRange(final byte min, final byte max) {
      return ByteRangeImpl.of(min, max);
   }

   static @NonNull DoubleRange doubleRange(final double min, final double max) {
      return DoubleRangeImpl.of(min, max);
   }

   static @NonNull FloatRange floatRange(final float min, final float max) {
      return FloatRangeImpl.of(min, max);
   }

   static @NonNull IntRange intRange(final int min, final int max) {
      return IntRangeImpl.of(min, max);
   }

   static @NonNull LongRange longRange(final long min, final long max) {
      return LongRangeImpl.of(min, max);
   }

   static @NonNull ShortRange shortRange(final short min, final short max) {
      return ShortRangeImpl.of(min, max);
   }
}
