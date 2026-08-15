package org.incendo.cloud.paper.util.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Source {
   @NonNull CommandSourceStack stack();

   @NonNull CommandSender source();
}
