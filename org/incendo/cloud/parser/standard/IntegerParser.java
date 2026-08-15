package org.incendo.cloud.parser.standard;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.NumberParseException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.type.range.IntRange;
import org.incendo.cloud.type.range.Range;

@API(status = Status.STABLE)
public final class IntegerParser<C> extends NumberParser<C, Integer, IntRange> implements BlockingSuggestionProvider.Strings<C> {
   @API(status = Status.STABLE)
   public static final int DEFAULT_MINIMUM = Integer.MIN_VALUE;
   @API(status = Status.STABLE)
   public static final int DEFAULT_MAXIMUM = Integer.MAX_VALUE;
   private static final int MAX_SUGGESTIONS_INCREMENT = 10;
   private static final int NUMBER_SHIFT_MULTIPLIER = 10;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Integer> integerParser() {
      return integerParser(Integer.MIN_VALUE, Integer.MAX_VALUE);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Integer> integerParser(final int minValue) {
      return ParserDescriptor.of(new IntegerParser<>(minValue, Integer.MAX_VALUE), Integer.class);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Integer> integerParser(final int minValue, final int maxValue) {
      return ParserDescriptor.of(new IntegerParser<>(minValue, maxValue), Integer.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, Integer> integerComponent() {
      return CommandComponent.<C, Integer>builder().parser(integerParser());
   }

   public IntegerParser(final int min, final int max) {
      super(Range.intRange(min, max));
   }

   public static @NonNull List<@NonNull String> getSuggestions(final @NonNull Range<? extends Number> range, final @NonNull CommandInput input) {
      Set<Long> numbers = new TreeSet<>();
      String token = input.peekString();

      try {
         long inputNum = Long.parseLong(token.equals("-") ? "-0" : (token.isEmpty() ? "0" : token));
         long inputNumAbsolute = Math.abs(inputNum);
         long min = range.min().longValue();
         long max = range.max().longValue();
         numbers.add(inputNumAbsolute);

         for (int i = 0; i < 10 && inputNum * 10L + i <= max; i++) {
            numbers.add(inputNumAbsolute * 10L + i);
         }

         List<String> suggestions = new LinkedList<>();

         for (long number : numbers) {
            if (token.startsWith("-")) {
               number = -number;
            }

            if (number >= min && number <= max) {
               suggestions.add(String.valueOf(number));
            }
         }

         return suggestions;
      } catch (Exception ignored) {
         return Collections.emptyList();
      }
   }

   @Override
   public @NonNull ArgumentParseResult<Integer> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      return !commandInput.isValidInteger(this.range())
         ? ArgumentParseResult.failure(new IntegerParser.IntegerParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readInteger());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxInt() != Integer.MAX_VALUE;
   }

   @Override
   public boolean hasMin() {
      return this.range().minInt() != Integer.MIN_VALUE;
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return getSuggestions(this.range(), input);
   }

   @API(status = Status.STABLE)
   public static final class IntegerParseException extends NumberParseException {
      @API(status = Status.STABLE)
      public IntegerParseException(final @NonNull String input, final @NonNull IntegerParser<?> parser, final @NonNull CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @Override
      public @NonNull String numberType() {
         return "integer";
      }
   }
}
