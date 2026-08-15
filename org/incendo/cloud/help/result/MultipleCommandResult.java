package org.incendo.cloud.help.result;

import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.help.HelpQuery;

@API(status = Status.STABLE)
@Immutable
public interface MultipleCommandResult<C> extends HelpQueryResult<C> {
   static <C> @NonNull MultipleCommandResult<C> of(
      final @NonNull HelpQuery<C> query, final @NonNull String longestPath, final @NonNull List<@NonNull String> childSuggestions
   ) {
      return MultipleCommandResultImpl.of(query, longestPath, childSuggestions);
   }

   @Override
   @NonNull HelpQuery<C> query();

   @NonNull String longestPath();

   @NonNull List<@NonNull String> childSuggestions();
}
