package org.incendo.cloud;

import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.meta.CommandMeta;

@API(status = Status.STABLE)
public interface CommandBuilderSource<C> {
   default Command.@NonNull Builder<C> commandBuilder(
      final @NonNull String name, final @NonNull Collection<String> aliases, final @NonNull Description description, final @NonNull CommandMeta meta
   ) {
      return this.decorateBuilder(Command.newBuilder(name, meta, description, aliases.toArray(new String[0])));
   }

   default Command.@NonNull Builder<C> commandBuilder(final @NonNull String name, final @NonNull Collection<String> aliases, final @NonNull CommandMeta meta) {
      return this.decorateBuilder(Command.newBuilder(name, meta, Description.empty(), aliases.toArray(new String[0])));
   }

   default Command.@NonNull Builder<C> commandBuilder(
      final @NonNull String name, final @NonNull CommandMeta meta, final @NonNull Description description, final @NonNull String... aliases
   ) {
      return this.decorateBuilder(Command.newBuilder(name, meta, description, aliases));
   }

   default Command.@NonNull Builder<C> commandBuilder(final @NonNull String name, final @NonNull CommandMeta meta, final @NonNull String... aliases) {
      return this.decorateBuilder(Command.newBuilder(name, meta, Description.empty(), aliases));
   }

   default Command.@NonNull Builder<C> commandBuilder(final @NonNull String name, final @NonNull Description description, final @NonNull String... aliases) {
      return this.decorateBuilder(Command.newBuilder(name, this.createDefaultCommandMeta(), description, aliases));
   }

   default Command.@NonNull Builder<C> commandBuilder(final @NonNull String name, final @NonNull String... aliases) {
      return this.decorateBuilder(Command.newBuilder(name, this.createDefaultCommandMeta(), Description.empty(), aliases));
   }

   @NonNull CommandMeta createDefaultCommandMeta();

   @API(status = Status.INTERNAL)
   Command.@NonNull Builder<C> decorateBuilder(Command.@NonNull Builder<C> builder);
}
