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
import org.incendo.cloud.type.range.DoubleRange;
import org.incendo.cloud.type.range.Range;

@API(status = Status.STABLE)
public final class DoubleParser<C> extends NumberParser<C, Double, DoubleRange> {
   @API(status = Status.STABLE)
   public static final double DEFAULT_MINIMUM = Double.NEGATIVE_INFINITY;
   @API(status = Status.STABLE)
   public static final double DEFAULT_MAXIMUM = Double.POSITIVE_INFINITY;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Double> doubleParser() {
      return doubleParser(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Double> doubleParser(final double minValue) {
      return ParserDescriptor.of(new DoubleParser<>(minValue, Double.POSITIVE_INFINITY), Double.class);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Double> doubleParser(final double minValue, final double maxValue) {
      return ParserDescriptor.of(new DoubleParser<>(minValue, maxValue), Double.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, Double> doubleComponent() {
      return CommandComponent.<C, Double>builder().parser(doubleParser());
   }

   public DoubleParser(final double min, final double max) {
      super(Range.doubleRange(min, max));
   }

   @Override
   public @NonNull ArgumentParseResult<Double> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      return !commandInput.isValidDouble(this.range())
         ? ArgumentParseResult.failure(new DoubleParser.DoubleParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readDouble());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxDouble() != Double.POSITIVE_INFINITY;
   }

   @Override
   public boolean hasMin() {
      return this.range().minDouble() != Double.NEGATIVE_INFINITY;
   }

   @API(status = Status.STABLE)
   public static final class DoubleParseException extends NumberParseException {
      @API(status = Status.STABLE)
      public DoubleParseException(final @NonNull String input, final @NonNull DoubleParser<?> parser, final @NonNull CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @Override
      public @NonNull String numberType() {
         return "double";
      }
   }
}
