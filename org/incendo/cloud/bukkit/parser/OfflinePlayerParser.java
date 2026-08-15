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

public final class OfflinePlayerParser<C> implements ArgumentParser<C, OfflinePlayer>, BlockingSuggestionProvider.Strings<C> {
   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, OfflinePlayer> offlinePlayerParser() {
      return ParserDescriptor.of(new OfflinePlayerParser<>(), OfflinePlayer.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, OfflinePlayer> offlinePlayerComponent() {
      return CommandComponent.<C, OfflinePlayer>builder().parser(offlinePlayerParser());
   }

   @Override
   public @NonNull ArgumentParseResult<OfflinePlayer> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String input = commandInput.readString();
      if (input.length() > 16) {
         return ArgumentParseResult.failure(new OfflinePlayerParser.OfflinePlayerParseException(input, commandContext));
      }

      OfflinePlayer player;
      try {
         player = Bukkit.getOfflinePlayer(input);
      } catch (Exception e) {
         return ArgumentParseResult.failure(new OfflinePlayerParser.OfflinePlayerParseException(input, commandContext));
      }

      return ArgumentParseResult.success(player);
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
      return Bukkit.getOnlinePlayers()
         .stream()
         .filter(player -> !(bukkit instanceof Player) || ((Player)bukkit).canSee(player))
         .<String>map(OfflinePlayer::getName)
         .collect(Collectors.toList());
   }

   public static final class OfflinePlayerParseException extends ParserException {
      private final String input;

      public OfflinePlayerParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
         super(OfflinePlayerParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER, CaptionVariable.of("input", input));
         this.input = input;
      }

      public @NonNull String input() {
         return this.input;
      }
   }
}
