package org.incendo.cloud.exception.parsing;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.NumberParser;
import org.incendo.cloud.type.range.Range;

@API(status = Status.STABLE)
public abstract class NumberParseException extends ParserException {
   private final String input;
   private final NumberParser<?, ?, ?> parser;

   protected NumberParseException(final @NonNull String input, final @NonNull NumberParser<?, ?, ?> parser, final @NonNull CommandContext<?> context) {
      super(
         parser.getClass(),
         context,
         StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER,
         CaptionVariable.of("input", input),
         CaptionVariable.of("min", String.valueOf(parser.range().min())),
         CaptionVariable.of("max", String.valueOf(parser.range().max()))
      );
      this.input = input;
      this.parser = parser;
   }

   public abstract @NonNull String numberType();

   public final @NonNull NumberParser<?, ?, ?> parser() {
      return this.parser;
   }

   public final boolean hasMax() {
      return this.parser.hasMax();
   }

   public final boolean hasMin() {
      return this.parser.hasMax();
   }

   public @NonNull String input() {
      return this.input;
   }

   public final @NonNull Range<? extends Number> range() {
      return (Range<? extends Number>)this.parser.range();
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         NumberParseException that = (NumberParseException)o;
         return this.parser().equals(that.parser());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.parser());
   }
}
