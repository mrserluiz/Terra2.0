package org.incendo.cloud.parser.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public final class LiteralParser<C> implements ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<C> {
   private final Set<String> allAcceptedAliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
   private final Set<String> alternativeAliases = new HashSet<>();
   private final String name;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, String> literal(final @NonNull String name, final @NonNull String @NonNull ... aliases) {
      return ParserDescriptor.of(new LiteralParser<>(name, aliases), String.class);
   }

   private LiteralParser(final @NonNull String name, final @NonNull String... aliases) {
      validateNames(name, aliases);
      this.name = name;
      this.allAcceptedAliases.add(this.name);
      this.allAcceptedAliases.addAll(Arrays.asList(aliases));
      this.alternativeAliases.addAll(Arrays.asList(aliases));
   }

   @Override
   public @NonNull ArgumentParseResult<String> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String string = commandInput.peekString();
      if (this.allAcceptedAliases.contains(string)) {
         commandInput.readString();
         return ArgumentParseResult.success(this.name);
      } else {
         return ArgumentParseResult.failure(new IllegalArgumentException(string));
      }
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return Collections.singletonList(this.name);
   }

   @API(status = Status.STABLE)
   public @NonNull Collection<@NonNull String> aliases() {
      return Collections.unmodifiableCollection(this.allAcceptedAliases);
   }

   @API(status = Status.STABLE)
   public @NonNull Collection<@NonNull String> alternativeAliases() {
      return Collections.unmodifiableCollection(this.alternativeAliases);
   }

   public void insertAlias(final @NonNull String alias) {
      validateNames("valid", new String[]{alias});
      this.allAcceptedAliases.add(alias);
      this.alternativeAliases.add(alias);
   }

   private static void validateNames(final String name, final String[] aliases) {
      List<String> errors = null;
      errors = validateName(name, false, errors);

      for (String alias : aliases) {
         errors = validateName(alias, true, errors);
      }

      if (errors != null && !errors.isEmpty()) {
         throw new IllegalArgumentException(String.join("\n", errors));
      }
   }

   private static @Nullable List<String> validateName(final @NonNull String name, final boolean alias, @Nullable List<String> errors) {
      int found = name.codePoints().filter(Character::isWhitespace).findFirst().orElse(Integer.MIN_VALUE);
      if (found != Integer.MIN_VALUE) {
         if (errors == null) {
            errors = new ArrayList<>();
         }

         errors.add(String.format("%s '%s' is invalid: contains whitespace", alias ? "Alias" : "Name", name));
      }

      return errors;
   }
}
