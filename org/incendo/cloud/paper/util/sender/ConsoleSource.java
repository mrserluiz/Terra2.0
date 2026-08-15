package org.incendo.cloud.paper.util.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.ConsoleCommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ConsoleSource extends GenericSource {
   ConsoleSource(final CommandSourceStack commandSourceStack) {
      super(commandSourceStack);
   }

   public @NonNull ConsoleCommandSender source() {
      return (ConsoleCommandSender)super.source();
   }
}
