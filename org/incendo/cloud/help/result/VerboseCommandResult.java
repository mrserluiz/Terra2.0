package org.incendo.cloud.help.result;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.help.HelpQuery;

@API(status = Status.STABLE)
@Immutable
public interface VerboseCommandResult<C> extends HelpQueryResult<C> {
   static <C> @NonNull VerboseCommandResult<C> of(final @NonNull HelpQuery<C> query, final @NonNull CommandEntry<C> entry) {
      return VerboseCommandResultImpl.of(query, entry);
   }

   @Override
   @NonNull HelpQuery<C> query();

   @NonNull CommandEntry<C> entry();
}
