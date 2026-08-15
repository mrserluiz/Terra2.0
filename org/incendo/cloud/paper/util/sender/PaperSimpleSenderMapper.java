package org.incendo.cloud.paper.util.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;

public final class PaperSimpleSenderMapper implements SenderMapper<CommandSourceStack, Source> {
   public static @NonNull PaperSimpleSenderMapper simpleSenderMapper() {
      return new PaperSimpleSenderMapper();
   }

   PaperSimpleSenderMapper() {
   }

   public @NonNull Source map(final @NonNull CommandSourceStack base) {
      CommandSender commandSender = base.getSender();
      if (commandSender instanceof ConsoleCommandSender) {
         return new ConsoleSource(base);
      } else if (commandSender instanceof Player) {
         return new PlayerSource(base);
      } else {
         return commandSender instanceof Entity ? new EntitySource(base) : new GenericSource(base);
      }
   }

   public @NonNull CommandSourceStack reverse(final @NonNull Source mapped) {
      return mapped.stack();
   }
}
