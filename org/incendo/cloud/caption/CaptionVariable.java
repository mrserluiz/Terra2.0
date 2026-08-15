package org.incendo.cloud.caption;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public interface CaptionVariable {
   static @NonNull CaptionVariable of(final @NonNull String key, final @NonNull String value) {
      return CaptionVariableImpl.of(key, value);
   }

   @NonNull String key();

   @NonNull String value();
}
