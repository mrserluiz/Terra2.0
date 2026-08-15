package org.incendo.cloud.util;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandInput;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class StringUtils {
   private StringUtils() {
   }

   public static int countCharOccurrences(final @NonNull String haystack, final char needle) {
      int occurrences = 0;

      for (int i = 0; i < haystack.length(); i++) {
         if (haystack.charAt(i) == needle) {
            occurrences++;
         }
      }

      return occurrences;
   }

   public static @NonNull String replaceAll(
      final @NonNull String string, final @NonNull Pattern pattern, final @NonNull Function<@NonNull MatchResult, @NonNull String> replacer
   ) {
      Matcher matcher = pattern.matcher(string);
      matcher.reset();
      boolean result = matcher.find();
      if (!result) {
         return string;
      }

      StringBuffer sb = new StringBuffer();

      do {
         String replacement = replacer.apply(matcher);
         matcher.appendReplacement(sb, replacement);
         result = matcher.find();
      } while (result);

      matcher.appendTail(sb);
      return sb.toString();
   }

   public static @Nullable String trimBeforeLastSpace(final String suggestion, final String input) {
      int lastSpace = input.lastIndexOf(32);
      if (lastSpace == -1) {
         return suggestion;
      } else {
         return suggestion.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT).substring(0, lastSpace))
            ? suggestion.substring(lastSpace + 1)
            : null;
      }
   }

   public static @Nullable String trimBeforeLastSpace(final String suggestion, final CommandInput commandInput) {
      String input;
      if (commandInput.isEmpty(true)) {
         input = "";
      } else {
         input = commandInput.copy().skipWhitespace().remainingInput();
      }

      return trimBeforeLastSpace(suggestion, input);
   }
}
