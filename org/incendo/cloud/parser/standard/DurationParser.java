package org.incendo.cloud.parser.standard;

import java.time.Duration;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

@API(status = Status.STABLE)
public final class DurationParser<C> implements ArgumentParser<C, Duration>, BlockingSuggestionProvider.Strings<C> {
   private static final Pattern DURATION_PATTERN = Pattern.compile("(([1-9][0-9]+|[1-9])[dhms])");

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Duration> durationParser() {
      return ParserDescriptor.of(new DurationParser<>(), Duration.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, Duration> durationComponent() {
      return CommandComponent.<C, Duration>builder().parser(durationParser());
   }

   @Override
   public @NonNull ArgumentParseResult<Duration> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String input = commandInput.readString();
      Matcher matcher = DURATION_PATTERN.matcher(input);
      Duration duration = Duration.ofNanos(0L);

      while (matcher.find()) {
         String group = matcher.group();
         String timeUnit = String.valueOf(group.charAt(group.length() - 1));
         int timeValue = Integer.parseInt(group.substring(0, group.length() - 1));
         switch (timeUnit) {
            case "d":
               duration = duration.plusDays(timeValue);
               break;
            case "h":
               duration = duration.plusHours(timeValue);
               break;
            case "m":
               duration = duration.plusMinutes(timeValue);
               break;
            case "s":
               duration = duration.plusSeconds(timeValue);
               break;
            default:
               return ArgumentParseResult.failure(new DurationParser.DurationParseException(input, commandContext));
         }
      }

      return duration.isZero()
         ? ArgumentParseResult.failure(new DurationParser.DurationParseException(input, commandContext))
         : ArgumentParseResult.success(duration);
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      if (input.isEmpty(true)) {
         return IntStream.range(1, 10).boxed().sorted().map(String::valueOf).collect(Collectors.toList());
      }

      if (Character.isLetter(input.lastRemainingCharacter())) {
         return Collections.emptyList();
      }

      String string = input.readString();
      return Stream.of("d", "h", "m", "s").filter(unit -> !string.contains(unit)).map(unit -> string + unit).collect(Collectors.toList());
   }

   @API(status = Status.STABLE)
   public static final class DurationParseException extends ParserException {
      private final String input;

      public DurationParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
         super(DurationParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION, CaptionVariable.of("input", input));
         this.input = input;
      }

      public @NonNull String input() {
         return this.input;
      }
   }
}
