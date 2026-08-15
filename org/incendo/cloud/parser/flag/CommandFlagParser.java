package org.incendo.cloud.parser.flag;

import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class CommandFlagParser<C> implements ArgumentParser.FutureArgumentParser<C, Object>, SuggestionProvider<C> {
   public static final Object FLAG_PARSE_RESULT_OBJECT = new Object();
   public static final CloudKey<String> FLAG_META_KEY = CloudKey.of("__last_flag__", TypeToken.get(String.class));
   public static final CloudKey<Integer> FLAG_CURSOR_KEY = CloudKey.of("__flag_cursor__", TypeToken.get(Integer.class));
   public static final CloudKey<Set<CommandFlag<?>>> PARSED_FLAGS = CloudKey.of("__parsed_flags__", new TypeToken<Set<CommandFlag<?>>>() {});
   private static final Pattern FLAG_PRIMARY_PATTERN = Pattern.compile(" --(?<name>([A-Za-z]+))");
   private static final Pattern FLAG_ALIAS_PATTERN = Pattern.compile(" -(?<name>([A-Za-z]+))");
   private final Collection<@NonNull CommandFlag<?>> flags;

   public CommandFlagParser(final @NonNull Collection<@NonNull CommandFlag<?>> flags) {
      this.flags = flags;
   }

   @API(status = Status.STABLE)
   public @NonNull Collection<@NonNull CommandFlag<?>> flags() {
      return Collections.unmodifiableCollection(this.flags);
   }

   @Override
   public @NonNull CompletableFuture<@NonNull ArgumentParseResult<Object>> parseFuture(
      final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput
   ) {
      return new CommandFlagParser.FlagParser().parse(commandContext, commandInput);
   }

   @API(status = Status.STABLE)
   public CompletableFuture<Optional<String>> parseCurrentFlag(
      final CommandContext<C> commandContext, final CommandInput commandInput, final Executor completionExecutor
   ) {
      if (commandInput.isEmpty()) {
         return CompletableFuture.completedFuture(Optional.empty());
      }

      String lastInputValue = commandInput.lastRemainingToken();
      CommandFlagParser<C>.FlagParser parser = new CommandFlagParser.FlagParser();
      CompletableFuture<ArgumentParseResult<Object>> result = parser.parse(commandContext, commandInput);
      return result.thenApplyAsync(parseResult -> {
         if (commandContext.contains(FLAG_CURSOR_KEY)) {
            commandInput.cursor(commandContext.get(FLAG_CURSOR_KEY));
         } else if (parser.lastParsedFlag() == null && commandInput.isEmpty()) {
            int count = lastInputValue.length();
            commandInput.moveCursor(-count);
         }

         return Optional.ofNullable(parser.lastParsedFlag());
      }, completionExecutor);
   }

   @Override
   public @NonNull CompletableFuture<Iterable<@NonNull Suggestion>> suggestionsFuture(
      final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input
   ) {
      String lastArg = Objects.requireNonNull(commandContext.getOrDefault(FLAG_META_KEY, ""));
      if (!lastArg.startsWith("-")) {
         String readInput = input.readInput();
         List<CommandFlag<?>> usedFlags = new LinkedList<>();
         Matcher primaryMatcher = FLAG_PRIMARY_PATTERN.matcher(readInput);

         while (primaryMatcher.find()) {
            String name = primaryMatcher.group("name");

            for (CommandFlag<?> flag : this.flags) {
               if (flag.name().equalsIgnoreCase(name)) {
                  usedFlags.add(flag);
                  break;
               }
            }
         }

         Matcher aliasMatcher = FLAG_ALIAS_PATTERN.matcher(readInput);

         while (aliasMatcher.find()) {
            String name = aliasMatcher.group("name");

            for (CommandFlag<?> flag : this.flags) {
               for (String alias : flag.aliases()) {
                  if (name.contains(alias)) {
                     usedFlags.add(flag);
                     break;
                  }
               }
            }
         }

         String nextToken = input.peekString();
         String currentFlag;
         if (nextToken.length() > 1) {
            currentFlag = nextToken.substring(1);
         } else {
            currentFlag = "";
         }

         List<Suggestion> suggestions = new LinkedList<>();

         for (CommandFlag<?> flag : this.flags) {
            if ((!usedFlags.contains(flag) || flag.mode() == CommandFlag.FlagMode.REPEATABLE) && commandContext.hasPermission(flag.permission())) {
               suggestions.add(Suggestion.suggestion(String.format("--%s", flag.name())));
            }
         }

         boolean suggestCombined = nextToken.length() > 1 && nextToken.startsWith("-") && !nextToken.startsWith("--");

         for (CommandFlag<?> flag : this.flags) {
            if ((!usedFlags.contains(flag) || flag.mode() == CommandFlag.FlagMode.REPEATABLE) && commandContext.hasPermission(flag.permission())) {
               for (String alias : flag.aliases()) {
                  if (!alias.equalsIgnoreCase(currentFlag)) {
                     if (suggestCombined && flag.commandComponent() == null) {
                        suggestions.add(Suggestion.suggestion(String.format("%s%s", input.peekString(), alias)));
                     } else {
                        suggestions.add(Suggestion.suggestion(String.format("-%s", alias)));
                     }
                  }
               }
            }
         }

         if (suggestCombined) {
            suggestions.add(Suggestion.suggestion(input.peekString()));
         }

         return CompletableFuture.completedFuture(suggestions);
      } else {
         CommandFlag<?> currentFlag = null;
         if (lastArg.startsWith("--")) {
            String flagName = lastArg.substring(2);

            for (CommandFlag<?> flag : this.flags) {
               if (flagName.equalsIgnoreCase(flag.name())) {
                  currentFlag = flag;
                  break;
               }
            }
         } else {
            String flagName = lastArg.substring(1);

            label166:
            for (CommandFlag<?> flag : this.flags) {
               for (String alias : flag.aliases()) {
                  if (alias.equalsIgnoreCase(flagName)) {
                     currentFlag = flag;
                     break label166;
                  }
               }
            }
         }

         if (currentFlag != null && commandContext.hasPermission(currentFlag.permission()) && currentFlag.commandComponent() != null) {
            SuggestionProvider suggestionProvider = currentFlag.commandComponent().suggestionProvider();
            return (CompletableFuture<Iterable<Suggestion>>)suggestionProvider.suggestionsFuture(commandContext, input);
         } else {
            commandContext.store(FLAG_META_KEY, "");
            return this.suggestionsFuture(commandContext, input);
         }
      }
   }

   @API(status = Status.STABLE)
   public enum FailureReason {
      UNKNOWN_FLAG(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG),
      DUPLICATE_FLAG(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG),
      NO_FLAG_STARTED(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED),
      MISSING_ARGUMENT(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT),
      NO_PERMISSION(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION);

      private final Caption caption;

      FailureReason(final @NonNull Caption caption) {
         this.caption = caption;
      }

      public @NonNull Caption caption() {
         return this.caption;
      }
   }

   @API(status = Status.STABLE)
   public static final class FlagParseException extends ParserException {
      private final String input;
      private final CommandFlagParser.FailureReason failureReason;

      public FlagParseException(
         final @NonNull String input, final CommandFlagParser.@NonNull FailureReason failureReason, final @NonNull CommandContext<?> context
      ) {
         super(CommandFlagParser.class, context, failureReason.caption(), CaptionVariable.of("input", input), CaptionVariable.of("flag", input));
         this.input = input;
         this.failureReason = failureReason;
      }

      public String input() {
         return this.input;
      }

      @API(status = Status.STABLE)
      public CommandFlagParser.@NonNull FailureReason failureReason() {
         return this.failureReason;
      }
   }

   private final class FlagParser {
      private String lastParsedFlag;

      private FlagParser() {
      }

      private @NonNull CompletableFuture<@NonNull ArgumentParseResult<Object>> parse(
         final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput
      ) {
         CompletableFuture<ArgumentParseResult<Object>> result = CompletableFuture.completedFuture(null);
         Set<CommandFlag<?>> parsedFlags = commandContext.computeIfAbsent(CommandFlagParser.PARSED_FLAGS, k -> new HashSet<>());
         int remainingTokens = commandInput.remainingTokens();

         for (int i = 0; i <= remainingTokens; i++) {
            result = result.thenCompose(
               parseResult -> {
                  commandInput.skipWhitespace();
                  if (parseResult == null && !commandInput.isEmpty()) {
                     String string = commandInput.peekString();
                     if (!string.startsWith("-")) {
                        return CompletableFuture.completedFuture(ArgumentParseResult.success(CommandFlagParser.FLAG_PARSE_RESULT_OBJECT));
                     }

                     this.lastParsedFlag = null;
                     if (string.startsWith("--")) {
                        commandInput.moveCursor(2);
                     } else {
                        commandInput.moveCursor(1);
                     }

                     String flagName = commandInput.readStringSkipWhitespace();
                     CommandFlag<?> flag = null;
                     if (string.startsWith("--")) {
                        for (CommandFlag<?> flagCandidate : CommandFlagParser.this.flags) {
                           if (flagName.equalsIgnoreCase(flagCandidate.name())) {
                              flag = flagCandidate;
                              break;
                           }
                        }
                     } else {
                        if (flagName.length() != 1) {
                           boolean flagFound = false;

                           for (int j = 0; j < flagName.length(); j++) {
                              String parsedFlag = Character.toString(flagName.charAt(j)).toLowerCase(Locale.ENGLISH);

                              for (CommandFlag<?> candidateFlag : CommandFlagParser.this.flags) {
                                 if (candidateFlag.commandComponent() == null && candidateFlag.aliases().contains(parsedFlag)) {
                                    if (parsedFlags.contains(candidateFlag) && candidateFlag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                                       return this.fail(
                                          new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.DUPLICATE_FLAG, commandContext)
                                       );
                                    }

                                    if (!commandContext.hasPermission(candidateFlag.permission())) {
                                       return this.fail(
                                          new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.NO_PERMISSION, commandContext)
                                       );
                                    }

                                    commandContext.flags().addPresenceFlag(candidateFlag);
                                    parsedFlags.add(candidateFlag);
                                    flagFound = true;
                                 }
                              }
                           }

                           if (!flagFound) {
                              return this.fail(
                                 new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.NO_FLAG_STARTED, commandContext)
                              );
                           }

                           return CompletableFuture.completedFuture(null);
                        }

                        label97:
                        for (CommandFlag<?> flagCandidate : CommandFlagParser.this.flags) {
                           for (String alias : flagCandidate.aliases()) {
                              if (alias.equalsIgnoreCase(flagName)) {
                                 flag = flagCandidate;
                                 break label97;
                              }
                           }
                        }
                     }

                     if (flag == null) {
                        return this.fail(new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.UNKNOWN_FLAG, commandContext));
                     }

                     if (parsedFlags.contains(flag) && flag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                        return this.fail(new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.DUPLICATE_FLAG, commandContext));
                     }

                     if (!commandContext.hasPermission(flag.permission())) {
                        return this.fail(new CommandFlagParser.FlagParseException(string, CommandFlagParser.FailureReason.NO_PERMISSION, commandContext));
                     }

                     if (flag.commandComponent() == null) {
                        commandContext.remove(CommandFlagParser.FLAG_CURSOR_KEY);
                        commandContext.flags().addPresenceFlag(flag);
                        parsedFlags.add(flag);
                        return CompletableFuture.completedFuture(null);
                     }

                     if (commandInput.hasRemainingInput() && commandInput.peek() == ' ') {
                        this.lastParsedFlag = string;
                     }

                     if (commandInput.isEmpty(true)) {
                        return this.fail(
                           new CommandFlagParser.FlagParseException(flag.name(), CommandFlagParser.FailureReason.MISSING_ARGUMENT, commandContext)
                        );
                     }

                     this.lastParsedFlag = string;
                     CommandFlag parsingFlag = flag;
                     CommandInput commandInputCopy = commandInput.copy();
                     return flag.commandComponent().parser().parseFuture(commandContext, commandInput).thenApply(parsedValue -> {
                        if (parsedValue.failure().isPresent() || commandInput.isEmpty() || commandInput.peek() != ' ') {
                           commandContext.store(CommandFlagParser.FLAG_CURSOR_KEY, commandInputCopy.cursor());
                        }

                        if (parsedValue.failure().isPresent()) {
                           return (ArgumentParseResult<Object>)parsedValue;
                        }

                        commandContext.flags().addValueFlag(parsingFlag, parsedValue.parsedValue().get());
                        parsedFlags.add(parsingFlag);
                        if (!commandInput.isEmpty(false) && commandInput.peek() == ' ') {
                           this.lastParsedFlag = null;
                        }

                        return null;
                     });
                  } else {
                     return CompletableFuture.completedFuture((ArgumentParseResult<Object>)parseResult);
                  }
               }
            );
         }

         return result.thenApply(r -> r == null ? ArgumentParseResult.success(CommandFlagParser.FLAG_PARSE_RESULT_OBJECT) : r);
      }

      private @Nullable String lastParsedFlag() {
         return this.lastParsedFlag;
      }

      private @NonNull CompletableFuture<ArgumentParseResult<Object>> fail(final @NonNull Throwable exception) {
         return ArgumentParseResult.failureFuture(exception);
      }
   }
}
