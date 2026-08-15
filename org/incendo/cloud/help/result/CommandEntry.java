package org.incendo.cloud.help.result;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.Command;

@API(status = Status.STABLE)
@Immutable
public interface CommandEntry<C> extends Comparable<CommandEntry<C>> {
   static <C> @NonNull CommandEntry<C> of(final @NonNull Command<C> command, final @NonNull String syntax) {
      return CommandEntryImpl.of(command, syntax);
   }

   @NonNull Command<C> command();

   @NonNull String syntax();

   default int compareTo(final @NonNull CommandEntry<C> other) {
      return this.syntax().compareTo(other.syntax());
   }
}
