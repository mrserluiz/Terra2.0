package org.incendo.cloud.meta;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.key.CloudKeyContainer;

@API(status = Status.STABLE)
public abstract class CommandMeta implements CloudKeyContainer {
   public static @NonNull CommandMetaBuilder builder() {
      return new CommandMetaBuilder();
   }

   @API(status = Status.STABLE)
   public static @NonNull CommandMeta empty() {
      return builder().build();
   }

   @Override
   public final @NonNull String toString() {
      return "";
   }
}
