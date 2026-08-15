package org.incendo.cloud.services;

import io.leangen.geantyref.TypeToken;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.services.type.Service;

enum ServiceFilterHandler {
   INSTANCE;

   <Context> boolean passes(final ServiceRepository.@NonNull ServiceWrapper<? extends Service<Context, ?>> service, final @NonNull Context context) {
      if (!service.isDefaultImplementation()) {
         for (Predicate<Context> predicate : service.filters()) {
            try {
               if (!predicate.test(context)) {
                  return false;
               }
            } catch (Exception e) {
               throw new PipelineException(
                  String.format("Failed to evaluate filter '%s' for '%s'", TypeToken.get(predicate.getClass()).getType().getTypeName(), service), e
               );
            }
         }
      }

      return true;
   }
}
