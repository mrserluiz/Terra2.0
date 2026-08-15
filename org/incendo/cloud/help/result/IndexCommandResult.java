package org.incendo.cloud.help.result;

import java.util.Iterator;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;
import org.incendo.cloud.help.HelpQuery;

@API(status = Status.STABLE)
@Immutable
public interface IndexCommandResult<C> extends HelpQueryResult<C>, Iterable<CommandEntry<C>> {
   static <C> @NonNull IndexCommandResult<C> of(final @NonNull HelpQuery<C> query, final @NonNull List<@NonNull CommandEntry<C>> entries) {
      return IndexCommandResultImpl.of(query, entries);
   }

   @Override
   @NonNull HelpQuery<C> query();

   @NonNull List<@NonNull CommandEntry<C>> entries();

   @Parameter(false)
   default boolean isEmpty() {
      return this.entries().isEmpty();
   }

   @Parameter(false)
   @Override
   default @NonNull Iterator<@NonNull CommandEntry<C>> iterator() {
      return this.entries().iterator();
   }
}
