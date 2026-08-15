package org.incendo.cloud.paper.util.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class PlayerSource extends EntitySource {
   PlayerSource(final CommandSourceStack commandSourceStack) {
      super(commandSourceStack);
   }

   public @NonNull Player source() {
      return (Player)super.source();
   }
}
