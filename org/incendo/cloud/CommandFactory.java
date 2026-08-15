package org.incendo.cloud;

import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
@FunctionalInterface
public interface CommandFactory<C> {
   @NonNull List<@NonNull Command<? extends C>> createCommands(@NonNull CommandManager<C> commandManager);
}
