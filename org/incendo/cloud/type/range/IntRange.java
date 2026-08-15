package org.incendo.cloud.type.range;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@Immutable
public interface IntRange extends Range<Integer> {
   int minInt();

   int maxInt();

   default @NonNull Integer min() {
      return this.minInt();
   }

   default @NonNull Integer max() {
      return this.maxInt();
   }
}
