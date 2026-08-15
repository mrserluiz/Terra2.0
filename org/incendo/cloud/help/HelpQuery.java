package org.incendo.cloud.help;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public interface HelpQuery<C> {
   static <C> @NonNull HelpQuery<C> of(final @NonNull C sender, final @NonNull String query) {
      return HelpQueryImpl.of(sender, query);
   }

   @NonNull C sender();

   @NonNull String query();
}
