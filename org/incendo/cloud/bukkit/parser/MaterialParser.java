package org.incendo.cloud.bukkit.parser;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Material;
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
import org.incendo.cloud.suggestion.Suggestion;

public final class MaterialParser<C> implements ArgumentParser<C, Material>, BlockingSuggestionProvider<C> {
   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, Material> materialParser() {
      return ParserDescriptor.of(new MaterialParser<>(), Material.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, Material> materialComponent() {
      return CommandComponent.<C, Material>builder().parser(materialParser());
   }

   @Override
   public @NonNull ArgumentParseResult<Material> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String input = commandInput.readString();

      try {
         Material material = Material.valueOf(input.toUpperCase(Locale.ROOT));
         return ArgumentParseResult.success(material);
      } catch (IllegalArgumentException exception) {
         return ArgumentParseResult.failure(new MaterialParser.MaterialParseException(input, commandContext));
      }
   }

   @Override
   public @NonNull Iterable<@NonNull Suggestion> suggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return Arrays.stream(Material.values()).map(Enum::name).map(String::toLowerCase).map(Suggestion::suggestion).collect(Collectors.toList());
   }

   public static final class MaterialParseException extends ParserException {
      private final String input;

      public MaterialParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
         super(MaterialParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_MATERIAL, CaptionVariable.of("input", input));
         this.input = input;
      }

      public @NonNull String input() {
         return this.input;
      }
   }
}
