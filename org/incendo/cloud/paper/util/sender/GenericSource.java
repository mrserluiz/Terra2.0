package org.incendo.cloud.paper.util.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

class GenericSource implements Source {
   private final CommandSourceStack commandSourceStack;

   GenericSource(final @NonNull CommandSourceStack commandSourceStack) {
      this.commandSourceStack = commandSourceStack;
   }

   @Override
   public final @NonNull CommandSourceStack stack() {
      return this.commandSourceStack;
   }

   @Override
   public @NonNull CommandSender source() {
      return this.commandSourceStack.getSender();
   }
}
