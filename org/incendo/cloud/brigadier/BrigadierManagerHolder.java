package org.incendo.cloud.brigadier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE, since = "2.0.0")
public interface BrigadierManagerHolder<C, S> {
   @API(status = Status.STABLE, since = "2.0.0")
   boolean hasBrigadierManager();

   @API(status = Status.STABLE, since = "2.0.0")
   @NonNull CloudBrigadierManager<C, ? extends S> brigadierManager();

   @API(status = Status.STABLE, since = "2.0.0")
   final class BrigadierManagerNotPresent extends RuntimeException {
      public BrigadierManagerNotPresent(final String message) {
         super(message);
      }
   }
}
