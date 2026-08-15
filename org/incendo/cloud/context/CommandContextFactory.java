package org.incendo.cloud.context;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface CommandContextFactory<C> {
   @API(status = Status.STABLE)
   @NonNull CommandContext<C> create(boolean suggestions, @NonNull C sender);
}
