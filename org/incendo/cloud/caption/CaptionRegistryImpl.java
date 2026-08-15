package org.incendo.cloud.caption;

import java.util.LinkedList;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

@API(status = Status.INTERNAL)
public final class CaptionRegistryImpl<C> implements CaptionRegistry<C> {
   private final LinkedList<@NonNull CaptionProvider<C>> providers = new LinkedList<>();

   CaptionRegistryImpl() {
   }

   @Override
   public @NonNull String caption(final @NonNull Caption caption, final @NonNull C sender) {
      for (CaptionProvider<C> provider : this.providers) {
         String result = provider.provide(caption, sender);
         if (result != null) {
            return result;
         }
      }

      throw new IllegalArgumentException(String.format("There is no caption stored with key '%s'", caption));
   }

   @Override
   public @This @NonNull CaptionRegistry<C> registerProvider(final @NonNull CaptionProvider<C> provider) {
      this.providers.addFirst(provider);
      return this;
   }
}
