package org.incendo.cloud.suggestion;

import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

@API(status = Status.STABLE)
@Immutable
public interface Suggestions<C, S extends Suggestion> {
   @NonNull CommandContext<C> commandContext();

   @NonNull List<S> list();

   @NonNull CommandInput commandInput();

   @API(status = Status.INTERNAL)
   static <C, S extends Suggestion> Suggestions<C, S> create(
      final @NonNull CommandContext<C> ctx, final @NonNull List<S> list, final @NonNull CommandInput commandInput
   ) {
      return SuggestionsImpl.of(ctx, list, commandInput);
   }
}
