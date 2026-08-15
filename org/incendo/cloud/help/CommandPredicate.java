package org.incendo.cloud.help;

import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;

@FunctionalInterface
@API(status = Status.STABLE)
public interface CommandPredicate<C> extends Predicate<Command<C>> {
   static <C> @NonNull CommandPredicate<C> acceptAll() {
      return cmd -> true;
   }

   boolean test(@NonNull Command<C> command);
}
