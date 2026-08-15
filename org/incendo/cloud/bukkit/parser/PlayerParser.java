package org.incendo.cloud.bukkit.parser;

import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

public final class PlayerParser<C> implements ArgumentParser<C, Player>, BlockingSuggestionProvider<C> {
   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, Player> playerParser() {
      return ParserDescriptor.of(new PlayerParser<>(), Player.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, Player> playerComponent() {
      return CommandComponent.<C, Player>builder().parser(playerParser());
   }

   @Override
   public @NonNull ArgumentParseResult<Player> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String input = commandInput.readString();
      Player player = Bukkit.getPlayer(input);
      return player == null ? ArgumentParseResult.failure(new PlayerParser.PlayerParseException(input, commandContext)) : ArgumentParseResult.success(player);
   }

   @Override
   public @NonNull Iterable<@NonNull Suggestion> suggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
      return Bukkit.getOnlinePlayers()
         .stream()
         .filter(player -> !(bukkit instanceof Player) || ((Player)bukkit).canSee(player))
         .<String>map(OfflinePlayer::getName)
         .map(Suggestion::suggestion)
         .collect(Collectors.toList());
   }

   public static final class PlayerParseException extends ParserException {
      private final String input;

      public PlayerParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
         super(PlayerParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER, CaptionVariable.of("input", input));
         this.input = input;
      }

      public @NonNull String input() {
         return this.input;
      }
   }
}
