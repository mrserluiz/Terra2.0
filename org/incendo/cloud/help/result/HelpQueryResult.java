package org.incendo.cloud.help.result;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.help.HelpQuery;
import org.incendo.cloud.help.HelpRenderer;

@API(status = Status.STABLE)
public interface HelpQueryResult<C> {
   @NonNull HelpQuery<C> query();

   default void render(final @NonNull HelpRenderer<C> renderer) {
      renderer.render(this);
   }
}
