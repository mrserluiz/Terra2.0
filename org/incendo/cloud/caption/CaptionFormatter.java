package org.incendo.cloud.caption;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface CaptionFormatter<C, T> {
   static <C> @NonNull CaptionFormatter<C, String> patternReplacing(final @NonNull Pattern pattern) {
      return new CaptionFormatter.PatternReplacingCaptionFormatter<>(pattern);
   }

   static <C> @NonNull CaptionFormatter<C, String> placeholderReplacing() {
      return new CaptionFormatter.PatternReplacingCaptionFormatter<>(placeholderPattern());
   }

   static Pattern placeholderPattern() {
      return Pattern.compile("<(\\S+)>");
   }

   default @NonNull T formatCaption(@NonNull Caption captionKey, @NonNull C recipient, @NonNull String caption, @NonNull CaptionVariable @NonNull ... variables) {
      return this.formatCaption(captionKey, recipient, caption, Arrays.asList(variables));
   }

   @NonNull T formatCaption(@NonNull Caption captionKey, @NonNull C recipient, @NonNull String caption, @NonNull List<@NonNull CaptionVariable> variables);

   final class PatternReplacingCaptionFormatter<C> implements CaptionFormatter<C, String> {
      private final Pattern pattern;

      private PatternReplacingCaptionFormatter(final @NonNull Pattern pattern) {
         this.pattern = pattern;
      }

      public @NonNull String formatCaption(
         final @NonNull Caption captionKey, final @NonNull C recipient, final @NonNull String caption, final @NonNull List<@NonNull CaptionVariable> variables
      ) {
         Map<String, String> replacements = new HashMap<>();

         for (CaptionVariable variable : variables) {
            replacements.put(variable.key(), variable.value());
         }

         Matcher matcher = this.pattern.matcher(caption);
         StringBuffer stringBuffer = new StringBuffer();

         while (matcher.find()) {
            String replacement = replacements.get(matcher.group(1));
            matcher.appendReplacement(stringBuffer, replacement == null ? "$0" : replacement);
         }

         matcher.appendTail(stringBuffer);
         return stringBuffer.toString();
      }
   }
}
