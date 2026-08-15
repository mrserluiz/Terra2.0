package org.incendo.cloud.services.type;

import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.services.ChunkedRequestContext;

public interface PartialResultService<Context, Result, Chunked extends ChunkedRequestContext<Context, Result>> extends Service<Chunked, Map<Context, Result>> {
   default @Nullable Map<@NonNull Context, @NonNull Result> handle(final @NonNull Chunked context) {
      if (!context.isCompleted()) {
         this.handleRequests(context.remaining()).forEach(context::storeResult);
      }

      return context.isCompleted() ? context.availableResults() : null;
   }

   @NonNull Map<@NonNull Context, @NonNull Result> handleRequests(@NonNull List<Context> requests);
}
