package org.incendo.cloud.bean;

import java.util.Arrays;
import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public interface CommandProperties {
   static @NonNull CommandProperties of(final @NonNull String name, final @NonNull String @NonNull ... aliases) {
      return CommandPropertiesImpl.of(name, Arrays.asList(aliases));
   }

   static @NonNull CommandProperties commandProperties(final @NonNull String name, final @NonNull String @NonNull ... aliases) {
      return of(name, aliases);
   }

   @NonNull String name();

   @NonNull Collection<@NonNull String> aliases();
}
