package org.incendo.cloud.caption;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

@API(status = Status.STABLE)
public interface CaptionRegistry<C> {
   @NonNull String caption(@NonNull Caption caption, @NonNull C sender);

   @This @NonNull CaptionRegistry<C> registerProvider(@NonNull CaptionProvider<C> provider);

   static <C> CaptionRegistry<C> captionRegistry() {
      return new CaptionRegistryImpl<>();
   }
}
