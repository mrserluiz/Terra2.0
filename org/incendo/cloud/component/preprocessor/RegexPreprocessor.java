package org.incendo.cloud.component.preprocessor;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;

@API(status = Status.STABLE)
public final class RegexPreprocessor<C> implements ComponentPreprocessor<C> {
   private final String rawPattern;
   private final Predicate<@NonNull String> predicate;
   private final Caption failureCaption;

   private RegexPreprocessor(final @NonNull String pattern, final @NonNull Caption failureCaption) {
      this.rawPattern = pattern;
      this.predicate = Pattern.compile(pattern).asPredicate();
      this.failureCaption = failureCaption;
   }

   public static <C> @NonNull RegexPreprocessor<C> of(final @NonNull String pattern) {
      return of(pattern, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_REGEX);
   }

   public static <C> @NonNull RegexPreprocessor<C> of(final @NonNull String pattern, final @NonNull Caption failureCaption) {
      return new RegexPreprocessor<>(pattern, failureCaption);
   }

   @Override
   public @NonNull ArgumentParseResult<Boolean> preprocess(final @NonNull CommandContext<C> context, final @NonNull CommandInput commandInput) {
      String head = commandInput.peekString();
      return this.predicate.test(head)
         ? ArgumentParseResult.success(true)
         : ArgumentParseResult.failure(new RegexPreprocessor.RegexValidationException(this.rawPattern, head, this.failureCaption, context));
   }

   @API(status = Status.STABLE)
   public static final class RegexValidationException extends IllegalArgumentException {
      private final String pattern;
      private final String failedString;
      private final Caption failureCaption;
      private final CommandContext<?> commandContext;

      private RegexValidationException(
         final @NonNull String pattern,
         final @NonNull String failedString,
         final @NonNull Caption failureCaption,
         final @NonNull CommandContext<?> commandContext
      ) {
         this.pattern = pattern;
         this.failedString = failedString;
         this.failureCaption = failureCaption;
         this.commandContext = commandContext;
      }

      @Override
      public String getMessage() {
         return this.commandContext
            .formatCaption(this.failureCaption, CaptionVariable.of("input", this.failedString), CaptionVariable.of("pattern", this.pattern));
      }

      public @NonNull String failedInput() {
         return this.failedString;
      }

      public @NonNull String pattern() {
         return this.pattern;
      }
   }
}
