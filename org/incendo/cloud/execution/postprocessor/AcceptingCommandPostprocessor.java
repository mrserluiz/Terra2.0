package org.incendo.cloud.execution.postprocessor;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class AcceptingCommandPostprocessor<C> implements CommandPostprocessor<C> {
   public static final String PROCESSED_INDICATOR_KEY = "__COMMAND_POST_PROCESSED__";

   public void accept(final @NonNull CommandPostprocessingContext<C> context) {
      context.commandContext().store("__COMMAND_POST_PROCESSED__", "true");
   }
}
