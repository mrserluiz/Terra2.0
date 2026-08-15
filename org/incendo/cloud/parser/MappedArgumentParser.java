package org.incendo.cloud.parser;

import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
public interface MappedArgumentParser<C, I, O> extends ArgumentParser<C, O> {
   @NonNull ArgumentParser<C, I> baseParser();

   @FunctionalInterface
   interface Mapper<C, I, O> {
      @NonNull CompletableFuture<ArgumentParseResult<O>> map(@NonNull CommandContext<C> context, @NonNull ArgumentParseResult<I> input);
   }
}
