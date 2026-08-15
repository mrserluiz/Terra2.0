package org.incendo.cloud.paper.parser;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.MappedArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = Status.EXPERIMENTAL)
public final class RegistryEntryParser<C, E extends Keyed>
   implements ArgumentParser<C, RegistryEntryParser.RegistryEntry<E>>,
   SuggestionProvider<C>,
   MappedArgumentParser<C, NamespacedKey, RegistryEntryParser.RegistryEntry<E>> {
   private final ParserDescriptor<C, NamespacedKey> keyParser = NamespacedKeyParser.namespacedKeyParser();
   private final RegistryKey<E> registryKey;

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C, E extends Keyed> @NonNull ParserDescriptor<C, RegistryEntryParser.RegistryEntry<E>> registryEntryParser(
      final RegistryKey<E> registryKey, final TypeToken<E> elementType
   ) {
      return ParserDescriptor.of(
         new RegistryEntryParser<>(registryKey),
         (TypeToken<RegistryEntryParser.RegistryEntry<E>>)TypeToken.get(
            TypeFactory.parameterizedClass(RegistryEntryParser.RegistryEntry.class, elementType.getType())
         )
      );
   }

   public RegistryEntryParser(final RegistryKey<E> registryKey) {
      this.registryKey = registryKey;
   }

   @Override
   public @NonNull ArgumentParseResult<RegistryEntryParser.RegistryEntry<@NonNull E>> parse(
      final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput
   ) {
      return this.keyParser
         .parser()
         .parse(commandContext, commandInput)
         .flatMapSuccess(
            key -> {
               Registry<E> registry = RegistryAccess.registryAccess().getRegistry(this.registryKey);
               E value = (E)registry.get(key);
               return value == null
                  ? ArgumentParseResult.failure(new RegistryEntryParser.ParseException(key.asString(), this.registryKey, commandContext))
                  : ArgumentParseResult.success(RegistryEntryImpl.of(value, key));
            }
         );
   }

   @Override
   public @NonNull ArgumentParser<C, NamespacedKey> baseParser() {
      return this.keyParser.parser();
   }

   @Override
   public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(
      final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input
   ) {
      List<Suggestion> completions = new ArrayList<>();
      Registry<E> registry = RegistryAccess.registryAccess().getRegistry(this.registryKey);
      registry.stream().<NamespacedKey>map(registry::getKeyOrThrow).forEach(key -> {
         if (input.hasRemainingInput() && key.getNamespace().equals("minecraft")) {
            completions.add(Suggestion.suggestion(key.getKey()));
         }

         completions.add(Suggestion.suggestion(key.getNamespace() + ':' + key.getKey()));
      });
      return CompletableFuture.completedFuture(completions);
   }

   public static final class ParseException extends ParserException {
      private final String input;
      private final RegistryKey<Object> registryKey;

      public ParseException(final @NonNull String input, final @NonNull RegistryKey<Object> registryKey, final @NonNull CommandContext<?> context) {
         super(
            RegistryEntryParser.class,
            context,
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_MISSING,
            CaptionVariable.of("input", input),
            CaptionVariable.of("registry", registryKey.key().asString())
         );
         this.input = input;
         this.registryKey = registryKey;
      }

      public @NonNull String input() {
         return this.input;
      }

      public @NonNull RegistryKey<Object> registryKey() {
         return this.registryKey;
      }
   }

   @Immutable
   public interface RegistryEntry<E> {
      E value();

      NamespacedKey key();
   }
}
