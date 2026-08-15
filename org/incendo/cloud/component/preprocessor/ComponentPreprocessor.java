package org.incendo.cloud.component.preprocessor;

import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;

@API(status = Status.STABLE)
@FunctionalInterface
public interface ComponentPreprocessor<C> {
   static <C> @NonNull ComponentPreprocessor<C> wrap(
      final @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull CommandInput, @NonNull ArgumentParseResult<Boolean>> function
   ) {
      return function::apply;
   }

   @NonNull ArgumentParseResult<Boolean> preprocess(@NonNull CommandContext<C> context, @NonNull CommandInput commandInput);
}
