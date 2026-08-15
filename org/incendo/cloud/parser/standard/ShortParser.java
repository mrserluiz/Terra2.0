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
import org.incendo.cloud.type.range.Range;
import org.incendo.cloud.type.range.ShortRange;

@API(status = Status.STABLE)
public final class ShortParser<C> extends NumberParser<C, Short, ShortRange> implements BlockingSuggestionProvider.Strings<C> {
   @API(status = Status.STABLE)
   public static final short DEFAULT_MINIMUM = -32768;
   @API(status = Status.STABLE)
   public static final short DEFAULT_MAXIMUM = 32767;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Short> shortParser() {
      return shortParser((short)-32768, (short)32767);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Short> shortParser(final short minValue) {
      return ParserDescriptor.of(new ShortParser<>(minValue, (short)32767), Short.class);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Short> shortParser(final short minValue, final short maxValue) {
      return ParserDescriptor.of(new ShortParser<>(minValue, maxValue), Short.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, Short> shortComponent() {
      return CommandComponent.<C, Short>builder().parser(shortParser());
   }

   public ShortParser(final short min, final short max) {
      super(Range.shortRange(min, max));
   }

   @Override
   public @NonNull ArgumentParseResult<Short> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      return !commandInput.isValidShort(this.range())
         ? ArgumentParseResult.failure(new ShortParser.ShortParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readShort());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxShort() != 32767;
   }

   @Override
   public boolean hasMin() {
      return this.range().minShort() != -32768;
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return IntegerParser.getSuggestions(this.range(), input);
   }

   @API(status = Status.STABLE)
   public static final class ShortParseException extends NumberParseException {
      @API(status = Status.STABLE)
      public ShortParseException(final @NonNull String input, final @NonNull ShortParser<?> parser, final @NonNull CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @Override
      public @NonNull String numberType() {
         return "short";
      }
   }
}
