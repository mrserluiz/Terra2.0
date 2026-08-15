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
import org.incendo.cloud.type.range.ByteRange;
import org.incendo.cloud.type.range.Range;

@API(status = Status.STABLE)
public final class ByteParser<C> extends NumberParser<C, Byte, ByteRange> implements BlockingSuggestionProvider.Strings<C> {
   @API(status = Status.STABLE)
   public static final byte DEFAULT_MINIMUM = -128;
   @API(status = Status.STABLE)
   public static final byte DEFAULT_MAXIMUM = 127;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Byte> byteParser() {
      return byteParser((byte)-128, (byte)127);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Byte> byteParser(final byte minValue) {
      return ParserDescriptor.of(new ByteParser<>(minValue, (byte)127), Byte.class);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Byte> byteParser(final byte minValue, final byte maxValue) {
      return ParserDescriptor.of(new ByteParser<>(minValue, maxValue), Byte.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, Byte> byteComponent() {
      return CommandComponent.<C, Byte>builder().parser(byteParser());
   }

   public ByteParser(final byte min, final byte max) {
      super(Range.byteRange(min, max));
   }

   @Override
   public @NonNull ArgumentParseResult<Byte> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      return !commandInput.isValidByte(this.range())
         ? ArgumentParseResult.failure(new ByteParser.ByteParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readByte());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxByte() != 127;
   }

   @Override
   public boolean hasMin() {
      return this.range().minByte() != -128;
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return IntegerParser.getSuggestions(this.range(), input);
   }

   @API(status = Status.STABLE)
   public static final class ByteParseException extends NumberParseException {
      @API(status = Status.STABLE)
      public ByteParseException(final @NonNull String input, final @NonNull ByteParser<?> parser, final @NonNull CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @Override
      public @NonNull String numberType() {
         return "byte";
      }
   }
}
