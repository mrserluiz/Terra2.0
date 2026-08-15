package org.incendo.cloud.services.type;

import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.services.ExecutionOrder;
import org.incendo.cloud.services.PipelineException;

@FunctionalInterface
public interface Service<Context, Result> extends Function<Context, Result> {
   @Nullable Result handle(@NonNull Context context) throws Exception;

   @Override
   default @Nullable Result apply(@NonNull Context context) {
      try {
         return this.handle(context);
      } catch (Exception exception) {
         throw new PipelineException(exception);
      }
   }

   default @Nullable ExecutionOrder order() {
      return null;
   }
}
