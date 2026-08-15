package org.incendo.cloud.brigadier.util;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.INTERNAL)
public final class BrigadierUtil {
   private BrigadierUtil() {
   }

   public static <S> LiteralCommandNode<S> buildRedirect(final @NonNull String alias, final @NonNull CommandNode<S> destination) {
      LiteralArgumentBuilder<S> builder = (LiteralArgumentBuilder<S>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal(alias)
               .requires(destination.getRequirement()))
            .forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork()))
         .executes(destination.getCommand());

      for (CommandNode<S> child : destination.getChildren()) {
         builder.then(child);
      }

      return builder.build();
   }
}
