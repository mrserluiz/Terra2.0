package org.incendo.cloud.parser.aggregate;

import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ArgumentParseResult;

@API(status = Status.STABLE)
public interface AggregateResultMapper<C, O> {
   @NonNull CompletableFuture<ArgumentParseResult<O>> map(@NonNull CommandContext<C> commandContext, @NonNull AggregateParsingContext<C> context);

   @API(status = Status.STABLE)
   interface DirectSuccessMapper<C, O> extends AggregateResultMapper<C, O> {
      @NonNull O mapSuccess(@NonNull CommandContext<C> commandContext, @NonNull AggregateParsingContext<C> context);

      @Override
      default @NonNull CompletableFuture<ArgumentParseResult<O>> map(@NonNull CommandContext<C> commandContext, @NonNull AggregateParsingContext<C> context) {
         return ArgumentParseResult.successFuture(this.mapSuccess(commandContext, context));
      }
   }
}
