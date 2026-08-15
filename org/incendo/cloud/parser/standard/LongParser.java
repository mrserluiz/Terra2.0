package org.incendo.cloud.parser.standard;

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
import org.incendo.cloud.type.range.LongRange;
import org.incendo.cloud.type.range.Range;

@API(status = Status.STABLE)
public final class LongParser<C> extends NumberParser<C, Long, LongRange> implements BlockingSuggestionProvider.Strings<C> {
   @API(status = Status.STABLE)
   public static final long DEFAULT_MINIMUM = Long.MIN_VALUE;
   @API(status = Status.STABLE)
   public static final long DEFAULT_MAXIMUM = Long.MAX_VALUE;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Long> longParser() {
      return longParser(Long.MIN_VALUE, Long.MAX_VALUE);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Long> longParser(final long minValue) {
      return ParserDescriptor.of(new LongParser<>(minValue, Long.MAX_VALUE), Long.class);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Long> longParser(final long minValue, final long maxValue) {
      return ParserDescriptor.of(new LongParser<>(minValue, maxValue), Long.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, Long> longComponent() {
      return CommandComponent.<C, Long>builder().parser(longParser());
   }

   public LongParser(final long min, final long max) {
      super(Range.longRange(min, max));
   }

   @Override
   public @NonNull ArgumentParseResult<Long> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      return !commandInput.isValidLong(this.range())
         ? ArgumentParseResult.failure(new LongParser.LongParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readLong());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxLong() != Long.MAX_VALUE;
   }

   @Override
   public boolean hasMin() {
      return this.range().minLong() != Long.MIN_VALUE;
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return IntegerParser.getSuggestions(this.range(), input);
   }

   @API(status = Status.STABLE)
   public static final class LongParseException extends NumberParseException {
      @API(status = Status.STABLE)
      public LongParseException(final @NonNull String input, final @NonNull LongParser<?> parser, final @NonNull CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @Override
      public @NonNull String numberType() {
         return "long";
      }
   }
}
