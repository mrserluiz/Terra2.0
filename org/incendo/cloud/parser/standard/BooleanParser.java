package org.incendo.cloud.parser.standard;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

@API(status = Status.STABLE)
public final class BooleanParser<C> implements ArgumentParser<C, Boolean>, BlockingSuggestionProvider.Strings<C> {
   private static final List<String> STRICT_LOWER = CommandInput.BOOLEAN_STRICT.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
   private static final List<String> LIBERAL_LOWER = CommandInput.BOOLEAN_LIBERAL.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
   private final boolean liberal;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Boolean> booleanParser() {
      return booleanParser(false);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Boolean> booleanParser(final boolean liberal) {
      return ParserDescriptor.of(new BooleanParser<>(liberal), Boolean.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, Boolean> booleanComponent() {
      return CommandComponent.<C, Boolean>builder().parser(booleanParser());
   }

   public BooleanParser(final boolean liberal) {
      this.liberal = liberal;
   }

   @Override
   public @NonNull ArgumentParseResult<Boolean> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      return !commandInput.isValidBoolean(this.liberal)
         ? ArgumentParseResult.failure(new BooleanParser.BooleanParseException(commandInput.peekString(), this.liberal, commandContext))
         : ArgumentParseResult.success(commandInput.readBoolean());
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return !this.liberal ? STRICT_LOWER : LIBERAL_LOWER;
   }

   @API(status = Status.STABLE)
   public static final class BooleanParseException extends ParserException {
      private final String input;
      private final boolean liberal;

      public BooleanParseException(final @NonNull String input, final boolean liberal, final @NonNull CommandContext<?> context) {
         super(BooleanParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_BOOLEAN, CaptionVariable.of("input", input));
         this.input = input;
         this.liberal = liberal;
      }

      public @NonNull String input() {
         return this.input;
      }

      public boolean liberal() {
         return this.liberal;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BooleanParser.BooleanParseException that = (BooleanParser.BooleanParseException)o;
            return this.liberal == that.liberal && this.input.equals(that.input);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.input, this.liberal);
      }
   }
}
