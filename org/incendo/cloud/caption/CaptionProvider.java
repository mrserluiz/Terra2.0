package org.incendo.cloud.caption;

import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.STABLE)
public interface CaptionProvider<C> {
   static <C> ImmutableConstantCaptionProvider.@NonNull Builder<C> constantProvider() {
      return ImmutableConstantCaptionProvider.builder();
   }

   static <C> @NonNull CaptionProvider<C> constantProvider(final @NonNull Caption caption, final @NonNull String value) {
      return constantProvider().putCaption(caption, value).build();
   }

   static <C> @NonNull CaptionProvider<C> forCaption(final @NonNull Caption caption, final @NonNull Function<@NonNull C, @Nullable String> provider) {
      return (key, recipient) -> key.equals(caption) ? provider.apply(recipient) : null;
   }

   @Nullable String provide(@NonNull Caption caption, @NonNull C recipient);
}
