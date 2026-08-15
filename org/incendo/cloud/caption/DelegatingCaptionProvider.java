package org.incendo.cloud.caption;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class DelegatingCaptionProvider<C> implements CaptionProvider<C> {
   public abstract @NonNull CaptionProvider<C> delegate();

   @Override
   public final @Nullable String provide(final @NonNull Caption caption, final @NonNull C recipient) {
      return this.delegate().provide(caption, recipient);
   }
}
