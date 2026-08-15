package org.incendo.cloud.type.range;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@Immutable
public interface FloatRange extends Range<Float> {
   float minFloat();

   float maxFloat();

   default @NonNull Float min() {
      return this.minFloat();
   }

   default @NonNull Float max() {
      return this.maxFloat();
   }
}
