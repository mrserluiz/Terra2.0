package org.incendo.cloud.bukkit;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import org.incendo.cloud.bukkit.internal.BukkitHelper;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessor;

final class BukkitCommandPreprocessor<C> implements CommandPreprocessor<C> {
   private final BukkitCommandManager<C> commandManager;
   private final @Nullable BukkitBackwardsBrigadierSenderMapper<C, ?> mapper;

   BukkitCommandPreprocessor(final @NonNull BukkitCommandManager<C> commandManager) {
      this.commandManager = commandManager;
      if (this.commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
         this.mapper = new BukkitBackwardsBrigadierSenderMapper<>(this.commandManager.senderMapper());
      } else {
         this.mapper = null;
      }
   }

   public void accept(final @NonNull CommandPreprocessingContext<C> context) {
      if (this.mapper != null && !context.commandContext().contains("_cloud_brigadier_native_sender")) {
         context.commandContext().store("_cloud_brigadier_native_sender", this.mapper.apply(context.commandContext().sender()));
      }

      context.commandContext()
         .store(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER, this.commandManager.senderMapper().reverse(context.commandContext().sender()));
      context.commandContext().computeIfAbsent(BukkitCommandContextKeys.SENDER_SCHEDULER_EXECUTOR, $ -> BukkitHelper.mainThreadExecutor(this.commandManager));
   }
}
