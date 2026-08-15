package org.incendo.cloud.type.range;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@Immutable
public interface ByteRange extends Range<Byte> {
   byte minByte();

   byte maxByte();

   default @NonNull Byte min() {
      return this.minByte();
   }

   default @NonNull Byte max() {
      return this.maxByte();
   }
}
