package org.incendo.cloud.bukkit.parser.selector;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserDescriptor;

public final class MultiplePlayerSelectorParser<C> extends SelectorUtils.PlayerSelectorParser<C, MultiplePlayerSelector> {
   private final boolean allowEmpty;

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, MultiplePlayerSelector> multiplePlayerSelectorParser() {
      return multiplePlayerSelectorParser(true);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, MultiplePlayerSelector> multiplePlayerSelectorParser(final boolean allowEmpty) {
      return ParserDescriptor.of(new MultiplePlayerSelectorParser<>(allowEmpty), MultiplePlayerSelector.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, MultiplePlayerSelector> multiplePlayerSelectorComponent() {
      return CommandComponent.<C, MultiplePlayerSelector>builder().parser(multiplePlayerSelectorParser());
   }

   @API(status = Status.STABLE, since = "1.8.0")
   public MultiplePlayerSelectorParser(final boolean allowEmpty) {
      super(false);
      this.allowEmpty = allowEmpty;
   }

   public MultiplePlayerSelectorParser() {
      this(true);
   }

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public MultiplePlayerSelector mapResult(final @NonNull String input, final SelectorUtils.@NonNull EntitySelectorWrapper wrapper) {
      final List<Player> players = wrapper.players();
      if (players.isEmpty() && !this.allowEmpty) {
         new SelectorUtils.SelectorParser.Thrower(NO_PLAYERS_EXCEPTION_TYPE.get()).throwIt();
      }

      return new MultiplePlayerSelector() {
         @Override
         public @NonNull String inputString() {
            return input;
         }

         @Override
         public @NonNull Collection<Player> values() {
            return Collections.unmodifiableCollection(players);
         }
      };
   }

   @Override
   protected CompletableFuture<ArgumentParseResult<MultiplePlayerSelector>> legacyParse(final CommandContext<C> commandContext, final CommandInput commandInput) {
      String input = commandInput.peekString();
      final Player player = Bukkit.getPlayer(input);
      if (player == null) {
         return CompletableFuture.completedFuture(ArgumentParseResult.failure(new PlayerParser.PlayerParseException(input, commandContext)));
      }

      final String pop = commandInput.readString();
      return ArgumentParseResult.successFuture(new MultiplePlayerSelector() {
         @Override
         public @NonNull String inputString() {
            return pop;
         }

         @Override
         public @NonNull Collection<Player> values() {
            return Collections.singletonList(player);
         }
      });
   }
}
