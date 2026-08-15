package org.incendo.cloud.bukkit.parser.selector;

import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.data.SinglePlayerSelector;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserDescriptor;

public final class SinglePlayerSelectorParser<C> extends SelectorUtils.PlayerSelectorParser<C, SinglePlayerSelector> {
   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, SinglePlayerSelector> singlePlayerSelectorParser() {
      return ParserDescriptor.of(new SinglePlayerSelectorParser<>(), SinglePlayerSelector.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, SinglePlayerSelector> singlePlayerSelectorComponent() {
      return CommandComponent.<C, SinglePlayerSelector>builder().parser(singlePlayerSelectorParser());
   }

   public SinglePlayerSelectorParser() {
      super(true);
   }

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public SinglePlayerSelector mapResult(final @NonNull String input, final SelectorUtils.@NonNull EntitySelectorWrapper wrapper) {
      final Player player = wrapper.singlePlayer();
      return new SinglePlayerSelector() {
         public @NonNull Player single() {
            return player;
         }

         @Override
         public @NonNull String inputString() {
            return input;
         }
      };
   }

   @Override
   protected CompletableFuture<ArgumentParseResult<SinglePlayerSelector>> legacyParse(final CommandContext<C> commandContext, final CommandInput commandInput) {
      String input = commandInput.peekString();
      final Player player = Bukkit.getPlayer(input);
      if (player == null) {
         return CompletableFuture.completedFuture(ArgumentParseResult.failure(new PlayerParser.PlayerParseException(input, commandContext)));
      }

      final String pop = commandInput.readString();
      return ArgumentParseResult.successFuture(new SinglePlayerSelector() {
         public @NonNull Player single() {
            return player;
         }

         @Override
         public @NonNull String inputString() {
            return pop;
         }
      });
   }
}
