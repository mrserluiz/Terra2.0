package org.incendo.cloud.caption;

import java.util.Map;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public abstract class ConstantCaptionProvider<C> implements CaptionProvider<C> {
   public abstract @NonNull Map<@NonNull Caption, @NonNull String> captions();

   @Override
   public final @Nullable String provide(final @NonNull Caption caption, final @NonNull C recipient) {
      return this.captions().get(caption);
   }
}
