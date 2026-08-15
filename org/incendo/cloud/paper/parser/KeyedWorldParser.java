package org.incendo.cloud.paper.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

public final class KeyedWorldParser<C> implements ArgumentParser<C, World>, SuggestionProvider<C> {
   private final ArgumentParser<C, World> parser;

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, World> keyedWorldParser() {
      return ParserDescriptor.of(new KeyedWorldParser<>(), World.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, World> keyedWorldComponent() {
      return CommandComponent.<C, World>builder().parser(keyedWorldParser());
   }

   public KeyedWorldParser() {
      Class<?> keyed = CraftBukkitReflection.findClass("org.bukkit.Keyed");
      if (keyed != null && keyed.isAssignableFrom(World.class)) {
         this.parser = null;
      } else {
         this.parser = new WorldParser<>();
      }
   }

   @Override
   public @NonNull ArgumentParseResult<@NonNull World> parse(final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput) {
      if (this.parser != null) {
         return this.parser.parse(commandContext, commandInput);
      }

      String input = commandInput.readString();
      NamespacedKey key = NamespacedKey.fromString(input);
      if (key == null) {
         return ArgumentParseResult.failure(new WorldParser.WorldParseException(input, commandContext));
      }

      World world = Bukkit.getWorld(key);
      return world == null ? ArgumentParseResult.failure(new WorldParser.WorldParseException(input, commandContext)) : ArgumentParseResult.success(world);
   }

   @Override
   public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(
      final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input
   ) {
      if (this.parser != null) {
         return this.parser.suggestionProvider().suggestionsFuture(commandContext, input);
      }

      List<World> worlds = Bukkit.getWorlds();
      List<Suggestion> completions = new ArrayList<>(worlds.size() * 2);

      for (World world : worlds) {
         NamespacedKey key = world.getKey();
         if (input.hasRemainingInput() && key.getNamespace().equals("minecraft")) {
            completions.add(Suggestion.suggestion(key.getKey()));
         }

         completions.add(Suggestion.suggestion(key.getNamespace() + ':' + key.getKey()));
      }

      return CompletableFuture.completedFuture(completions);
   }
}
