package org.incendo.cloud.help;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.help.result.HelpQueryResult;

@API(status = Status.STABLE)
public interface HelpRenderer<C> {
   void render(@NonNull HelpQueryResult<C> result);
}
