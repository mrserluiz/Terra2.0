package org.incendo.cloud.type.range;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@Immutable
public interface DoubleRange extends Range<Double> {
   double minDouble();

   double maxDouble();

   default @NonNull Double min() {
      return this.minDouble();
   }

   default @NonNull Double max() {
      return this.maxDouble();
   }
}
