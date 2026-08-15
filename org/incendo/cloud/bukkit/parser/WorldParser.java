package org.incendo.cloud.bukkit.parser;

import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public final class WorldParser<C> implements ArgumentParser<C, World>, BlockingSuggestionProvider.Strings<C> {
   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, World> worldParser() {
      return ParserDescriptor.of(new WorldParser<>(), World.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, World> worldComponent() {
      return CommandComponent.<C, World>builder().parser(worldParser());
   }

   @Override
   public @NonNull ArgumentParseResult<World> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String input = commandInput.readString();
      World world = Bukkit.getWorld(input);
      return world == null ? ArgumentParseResult.failure(new WorldParser.WorldParseException(input, commandContext)) : ArgumentParseResult.success(world);
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return Bukkit.getWorlds().stream().<String>map(World::getName).collect(Collectors.toList());
   }

   public static final class WorldParseException extends ParserException {
      private final String input;

      public WorldParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
         super(WorldParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD, CaptionVariable.of("input", input));
         this.input = input;
      }

      public @NonNull String input() {
         return this.input;
      }
   }
}
