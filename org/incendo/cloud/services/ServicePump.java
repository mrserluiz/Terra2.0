package org.incendo.cloud.services;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.services.type.Service;

public final class ServicePump<Context> {
   private final ServicePipeline servicePipeline;
   private final Context context;

   ServicePump(final @NonNull ServicePipeline servicePipeline, final @NonNull Context context) {
      this.servicePipeline = servicePipeline;
      this.context = context;
   }

   public <Result> @NonNull ServiceSpigot<@NonNull Context, @NonNull Result> through(
      final @NonNull TypeToken<? extends Service<@NonNull Context, @NonNull Result>> type
   ) {
      return new ServiceSpigot<>(this.servicePipeline, this.context, type);
   }

   public <Result> @NonNull ServiceSpigot<@NonNull Context, @NonNull Result> through(
      final @NonNull Class<? extends Service<@NonNull Context, @NonNull Result>> clazz
   ) {
      return this.through(TypeToken.get(clazz));
   }
}
