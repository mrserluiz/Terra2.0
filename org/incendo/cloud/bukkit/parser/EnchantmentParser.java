package org.incendo.cloud.bukkit.parser;

import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
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

public final class EnchantmentParser<C> implements ArgumentParser<C, Enchantment>, BlockingSuggestionProvider.Strings<C> {
   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, Enchantment> enchantmentParser() {
      return ParserDescriptor.of(new EnchantmentParser<>(), Enchantment.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, Enchantment> enchantmentComponent() {
      return CommandComponent.<C, Enchantment>builder().parser(enchantmentParser());
   }

   @Override
   public @NonNull ArgumentParseResult<Enchantment> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String input = commandInput.peekString();

      NamespacedKey key;
      try {
         if (input.contains(":")) {
            key = new NamespacedKey(commandInput.readUntilAndSkip(':'), commandInput.readString());
         } else {
            key = NamespacedKey.minecraft(commandInput.readString());
         }
      } catch (Exception ex) {
         return ArgumentParseResult.failure(new EnchantmentParser.EnchantmentParseException(input, commandContext));
      }

      Enchantment enchantment = Enchantment.getByKey(key);
      return enchantment == null
         ? ArgumentParseResult.failure(new EnchantmentParser.EnchantmentParseException(input, commandContext))
         : ArgumentParseResult.success(enchantment);
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      List<String> completions = new ArrayList<>();

      for (Enchantment value : Enchantment.values()) {
         if (value.getKey().getNamespace().equals("minecraft")) {
            completions.add(value.getKey().getKey());
         } else {
            completions.add(value.getKey().toString());
         }
      }

      return completions;
   }

   public static final class EnchantmentParseException extends ParserException {
      private final String input;

      public EnchantmentParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
         super(EnchantmentParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_ENCHANTMENT, CaptionVariable.of("input", input));
         this.input = input;
      }

      public @NonNull String input() {
         return this.input;
      }
   }
}
