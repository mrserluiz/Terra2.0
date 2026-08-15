package org.incendo.cloud.caption;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public interface Caption {
   static @NonNull Caption of(final @NonNull String key) {
      return CaptionImpl.of(key);
   }

   @NonNull String key();
}
