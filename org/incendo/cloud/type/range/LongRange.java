package org.incendo.cloud.type.range;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@Immutable
public interface LongRange extends Range<Long> {
   long minLong();

   long maxLong();

   default @NonNull Long min() {
      return this.minLong();
   }

   default @NonNull Long max() {
      return this.maxLong();
   }
}
