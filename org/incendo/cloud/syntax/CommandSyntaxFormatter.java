package org.incendo.cloud.syntax;

import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandNode;

@FunctionalInterface
@API(status = Status.STABLE)
public interface CommandSyntaxFormatter<C> {
   @NonNull String apply(@Nullable C sender, @NonNull List<@NonNull CommandComponent<C>> commandComponents, @Nullable CommandNode<C> node);
}
