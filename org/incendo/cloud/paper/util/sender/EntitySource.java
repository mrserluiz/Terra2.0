package org.incendo.cloud.paper.util.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EntitySource extends GenericSource {
   EntitySource(final CommandSourceStack commandSourceStack) {
      super(commandSourceStack);
   }

   public @NonNull Entity source() {
      return (Entity)super.source();
   }
}
