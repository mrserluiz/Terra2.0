package org.incendo.cloud.brigadier.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.permission.BrigadierPermissionChecker;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.brigadier.*")
public interface BrigadierNodeFactory<C, S, N extends CommandNode<S>> {
   @NonNull N createNode(
      @NonNull String label, @NonNull CommandNode<C> cloudCommand, @NonNull Command<S> executor, @NonNull BrigadierPermissionChecker<C> permissionChecker
   );

   @NonNull N createNode(
      @NonNull String label, @NonNull Command<C> cloudCommand, @NonNull Command<S> executor, @NonNull BrigadierPermissionChecker<C> permissionChecker
   );

   @NonNull N createNode(@NonNull String label, @NonNull Command<C> cloudCommand, @NonNull Command<S> executor);
}
