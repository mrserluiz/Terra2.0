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
import org.incendo.cloud.type.range.FloatRange;
import org.incendo.cloud.type.range.Range;

@API(status = Status.STABLE)
public final class FloatParser<C> extends NumberParser<C, Float, FloatRange> {
   @API(status = Status.STABLE)
   public static final float DEFAULT_MINIMUM = Float.NEGATIVE_INFINITY;
   @API(status = Status.STABLE)
   public static final float DEFAULT_MAXIMUM = Float.POSITIVE_INFINITY;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Float> floatParser() {
      return floatParser(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Float> floatParser(final float minValue) {
      return ParserDescriptor.of(new FloatParser<>(minValue, Float.POSITIVE_INFINITY), Float.class);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Float> floatParser(final float minValue, final float maxValue) {
      return ParserDescriptor.of(new FloatParser<>(minValue, maxValue), Float.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, Float> floatComponent() {
      return CommandComponent.<C, Float>builder().parser(floatParser());
   }

   public FloatParser(final float min, final float max) {
      super(Range.floatRange(min, max));
   }

   @Override
   public @NonNull ArgumentParseResult<Float> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      return !commandInput.isValidFloat(this.range())
         ? ArgumentParseResult.failure(new FloatParser.FloatParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readFloat());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxFloat() != Float.POSITIVE_INFINITY;
   }

   @Override
   public boolean hasMin() {
      return this.range().minFloat() != Float.NEGATIVE_INFINITY;
   }

   @API(status = Status.STABLE)
   public static final class FloatParseException extends NumberParseException {
      @API(status = Status.STABLE)
      public FloatParseException(final @NonNull String input, final @NonNull FloatParser<?> parser, final @NonNull CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @Override
      public @NonNull String numberType() {
         return "float";
      }
   }
}
