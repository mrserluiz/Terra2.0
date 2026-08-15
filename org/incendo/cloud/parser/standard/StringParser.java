package org.incendo.cloud.parser.standard;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.util.StringUtils;

@API(status = Status.STABLE)
public final class StringParser<C> implements ArgumentParser<C, String> {
   private static final Pattern QUOTED_DOUBLE = Pattern.compile("\"(?<inner>(?:[^\"\\\\]|\\\\.)*)\"");
   private static final Pattern QUOTED_SINGLE = Pattern.compile("'(?<inner>(?:[^'\\\\]|\\\\.)*)'");
   private static final Pattern FLAG_PATTERN = Pattern.compile("(-[A-Za-z_\\-0-9])|(--[A-Za-z_\\-0-9]*)");
   private final StringParser.StringMode stringMode;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, String> stringParser(final StringParser.@NonNull StringMode mode) {
      return ParserDescriptor.of(new StringParser<>(mode), String.class);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, String> stringParser() {
      return stringParser(StringParser.StringMode.SINGLE);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, String> greedyStringParser() {
      return stringParser(StringParser.StringMode.GREEDY);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, String> greedyFlagYieldingStringParser() {
      return stringParser(StringParser.StringMode.GREEDY_FLAG_YIELDING);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, String> quotedStringParser() {
      return stringParser(StringParser.StringMode.QUOTED);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, String> stringComponent(final StringParser.@NonNull StringMode mode) {
      return CommandComponent.<C, String>builder().parser(stringParser(mode));
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, String> stringComponent() {
      return CommandComponent.<C, String>builder().parser(stringParser(StringParser.StringMode.SINGLE));
   }

   public StringParser(final StringParser.@NonNull StringMode stringMode) {
      this.stringMode = stringMode;
   }

   @Override
   public @NonNull ArgumentParseResult<String> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      if (this.stringMode == StringParser.StringMode.SINGLE) {
         return ArgumentParseResult.success(commandInput.readString());
      } else {
         return this.stringMode == StringParser.StringMode.QUOTED ? this.parseQuoted(commandContext, commandInput) : this.parseGreedy(commandInput);
      }
   }

   private @NonNull ArgumentParseResult<String> parseQuoted(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      char peek = commandInput.peek();
      if (peek != '\'' && peek != '"') {
         return ArgumentParseResult.success(commandInput.readString());
      }

      String string = commandInput.remainingInput();
      Matcher doubleMatcher = QUOTED_DOUBLE.matcher(string);
      String doubleMatch = null;
      if (doubleMatcher.find()) {
         doubleMatch = doubleMatcher.group("inner");
      }

      Matcher singleMatcher = QUOTED_SINGLE.matcher(string);
      String singleMatch = null;
      if (singleMatcher.find()) {
         singleMatch = singleMatcher.group("inner");
      }

      String inner = null;
      if (singleMatch != null && doubleMatch != null) {
         int singleIndex = string.indexOf(singleMatch);
         int doubleIndex = string.indexOf(doubleMatch);
         inner = doubleIndex < singleIndex ? doubleMatch : singleMatch;
      } else if (singleMatch == null && doubleMatch != null) {
         inner = doubleMatch;
      } else if (singleMatch != null) {
         inner = singleMatch;
      }

      if (inner != null) {
         int numSpaces = StringUtils.countCharOccurrences(inner, ' ');

         for (int i = 0; i <= numSpaces; i++) {
            commandInput.readString();
         }
      } else {
         inner = commandInput.peekString();
         if (inner.startsWith("\"") || inner.startsWith("'")) {
            return ArgumentParseResult.failure(
               new StringParser.StringParseException(commandInput.remainingInput(), StringParser.StringMode.QUOTED, commandContext)
            );
         }

         commandInput.readString();
      }

      inner = inner.replace("\\\"", "\"").replace("\\'", "'");
      return ArgumentParseResult.success(inner);
   }

   private @NonNull ArgumentParseResult<String> parseGreedy(final @NonNull CommandInput commandInput) {
      int size = commandInput.remainingTokens();
      StringJoiner stringJoiner = new StringJoiner(" ");

      for (int i = 0; i < size; i++) {
         String string = commandInput.peekString();
         if (string.isEmpty() || this.stringMode == StringParser.StringMode.GREEDY_FLAG_YIELDING && FLAG_PATTERN.matcher(string).matches()) {
            break;
         }

         stringJoiner.add(commandInput.readStringSkipWhitespace(false));
      }

      return ArgumentParseResult.success(stringJoiner.toString());
   }

   public StringParser.@NonNull StringMode stringMode() {
      return this.stringMode;
   }

   @API(status = Status.STABLE)
   public enum StringMode {
      SINGLE,
      QUOTED,
      GREEDY,
      @API(status = Status.STABLE)
      GREEDY_FLAG_YIELDING;
   }

   @API(status = Status.STABLE)
   public static final class StringParseException extends ParserException {
      private final String input;
      private final StringParser.StringMode stringMode;

      public StringParseException(final @NonNull String input, final StringParser.@NonNull StringMode stringMode, final @NonNull CommandContext<?> context) {
         super(
            StringParser.class,
            context,
            StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_STRING,
            CaptionVariable.of("input", input),
            CaptionVariable.of("stringMode", stringMode.name())
         );
         this.input = input;
         this.stringMode = stringMode;
      }

      public @NonNull String input() {
         return this.input;
      }

      public StringParser.@NonNull StringMode stringMode() {
         return this.stringMode;
      }
   }
}
