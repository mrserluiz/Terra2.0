package org.incendo.cloud.bukkit.parser;

import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.bukkit.BukkitParserParameters;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public final class NamespacedKeyParser<C> implements ArgumentParser<C, NamespacedKey>, BlockingSuggestionProvider.Strings<C> {
   private final boolean requireExplicitNamespace;
   private final String defaultNamespace;

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, NamespacedKey> namespacedKeyParser() {
      return namespacedKeyParser(false);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, NamespacedKey> namespacedKeyParser(final boolean requireExplicitNamespace) {
      return namespacedKeyParser(requireExplicitNamespace, "minecraft");
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, NamespacedKey> namespacedKeyParser(
      final boolean requireExplicitNamespace, final @NonNull String defaultNamespace
   ) {
      return ParserDescriptor.of(new NamespacedKeyParser<>(requireExplicitNamespace, defaultNamespace), NamespacedKey.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, NamespacedKey> namespacedKeyComponent() {
      return CommandComponent.<C, NamespacedKey>builder().parser(namespacedKeyParser());
   }

   public NamespacedKeyParser(final boolean requireExplicitNamespace, final String defaultNamespace) {
      this.requireExplicitNamespace = requireExplicitNamespace;
      this.defaultNamespace = defaultNamespace;
   }

   @Override
   public @NonNull ArgumentParseResult<NamespacedKey> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String input = commandInput.peekString();
      String[] split = input.split(":");
      int maxSemi = split.length > 1 ? 1 : 0;
      if (input.length() - input.replace(":", "").length() > maxSemi) {
         return ArgumentParseResult.failure(
            new NamespacedKeyParser.NamespacedKeyParseException(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY, input, commandContext)
         );
      }

      try {
         NamespacedKey ret;
         if (split.length == 1) {
            if (this.requireExplicitNamespace) {
               return ArgumentParseResult.failure(
                  new NamespacedKeyParser.NamespacedKeyParseException(
                     BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE, input, commandContext
                  )
               );
            }

            ret = new NamespacedKey(this.defaultNamespace, commandInput.readString());
         } else {
            if (split.length != 2) {
               return ArgumentParseResult.failure(
                  new NamespacedKeyParser.NamespacedKeyParseException(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY, input, commandContext)
               );
            }

            ret = new NamespacedKey(commandInput.readUntilAndSkip(':'), commandInput.readString());
         }

         return ArgumentParseResult.success(ret);
      } catch (IllegalArgumentException ex) {
         Caption caption = ex.getMessage().contains("namespace")
            ? BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE
            : BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY;
         return ArgumentParseResult.failure(new NamespacedKeyParser.NamespacedKeyParseException(caption, input, commandContext));
      }
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      List<String> ret = new ArrayList<>();
      ret.add(this.defaultNamespace + ":");
      String token = input.peekString();
      if (!token.contains(":") && !token.isEmpty()) {
         ret.add(token + ":");
      }

      return ret;
   }

   private static <C> void registerParserSupplier(final @NonNull CommandManager<C> commandManager) {
      commandManager.parserRegistry()
         .registerParserSupplier(
            TypeToken.get(NamespacedKey.class),
            params -> new NamespacedKeyParser<>(
               params.has(BukkitParserParameters.REQUIRE_EXPLICIT_NAMESPACE), params.get(BukkitParserParameters.DEFAULT_NAMESPACE, "minecraft")
            )
         );
   }

   public static final class NamespacedKeyParseException extends ParserException {
      private final String input;

      public NamespacedKeyParseException(final @NonNull Caption caption, final @NonNull String input, final @NonNull CommandContext<?> context) {
         super(NamespacedKeyParser.class, context, caption, CaptionVariable.of("input", input));
         this.input = input;
      }

      public @NonNull String input() {
         return this.input;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            NamespacedKeyParser.NamespacedKeyParseException that = (NamespacedKeyParser.NamespacedKeyParseException)o;
            return this.input.equals(that.input) && this.errorCaption().equals(that.errorCaption());
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.input, this.errorCaption());
      }
   }
}
