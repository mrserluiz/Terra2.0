package org.incendo.cloud.type.range;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@Immutable
public interface ShortRange extends Range<Short> {
   short minShort();

   short maxShort();

   default @NonNull Short min() {
      return this.minShort();
   }

   default @NonNull Short max() {
      return this.maxShort();
   }
}
