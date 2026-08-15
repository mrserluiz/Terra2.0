package org.incendo.cloud.exception.parsing;

import java.util.Arrays;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionFormatter;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
public class ParserException extends IllegalArgumentException {
   private final Class<?> argumentParser;
   private final CommandContext<?> context;
   private final Caption errorCaption;
   private final CaptionVariable[] captionVariables;

   protected ParserException(
      final @Nullable Throwable cause,
      final @NonNull Class<?> argumentParser,
      final @NonNull CommandContext<?> context,
      final @NonNull Caption errorCaption,
      final @NonNull CaptionVariable... captionVariables
   ) {
      super(cause);
      this.argumentParser = argumentParser;
      this.context = context;
      this.errorCaption = errorCaption;
      this.captionVariables = captionVariables;
   }

   protected ParserException(
      final @NonNull Class<?> argumentParser,
      final @NonNull CommandContext<?> context,
      final @NonNull Caption errorCaption,
      final @NonNull CaptionVariable... captionVariables
   ) {
      this(null, argumentParser, context, errorCaption, captionVariables);
   }

   @Override
   public final String getMessage() {
      return this.context.formatCaption(this.errorCaption, this.captionVariables);
   }

   @API(status = Status.STABLE)
   public final <T> @NonNull T formatCaption(final @NonNull CaptionFormatter<?, T> formatter) {
      return this.context.formatCaption(formatter, this.errorCaption, this.captionVariables());
   }

   @API(status = Status.STABLE)
   public @NonNull Caption errorCaption() {
      return this.errorCaption;
   }

   @API(status = Status.STABLE)
   public @NonNull CaptionVariable @NonNull [] captionVariables() {
      return Arrays.copyOf(this.captionVariables, this.captionVariables.length);
   }

   public final @NonNull Class<?> argumentParserClass() {
      return this.argumentParser;
   }

   public final @NonNull CommandContext<?> context() {
      return this.context;
   }
}
