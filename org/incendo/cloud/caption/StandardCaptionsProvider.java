package org.incendo.cloud.caption;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public final class StandardCaptionsProvider<C> extends DelegatingCaptionProvider<C> {
   public static final String ARGUMENT_PARSE_FAILURE_BOOLEAN = "Could not parse boolean from '<input>'";
   public static final String ARGUMENT_PARSE_FAILURE_NUMBER = "'<input>' is not a valid number in the range <min> to <max>";
   public static final String ARGUMENT_PARSE_FAILURE_CHAR = "'<input>' is not a valid character";
   public static final String ARGUMENT_PARSE_FAILURE_ENUM = "'<input>' is not one of the following: <acceptableValues>";
   public static final String ARGUMENT_PARSE_FAILURE_STRING = "'<input>' is not a valid string of type <stringMode>";
   public static final String ARGUMENT_PARSE_FAILURE_UUID = "'<input>' is not a valid UUID";
   public static final String ARGUMENT_PARSE_FAILURE_REGEX = "'<input>' does not match '<pattern>'";
   public static final String ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG = "Unknown flag '<flag>'";
   public static final String ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG = "Duplicate flag '<flag>'";
   public static final String ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED = "No flag started. Don't know what to do with '<input>'";
   public static final String ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT = "Missing argument for '<flag>'";
   public static final String ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION = "You don't have permission to use '<flag>'";
   public static final String ARGUMENT_PARSE_FAILURE_COLOR = "'<input>' is not a valid color";
   public static final String ARGUMENT_PARSE_FAILURE_DURATION = "'<input>' is not a duration format";
   public static final String ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT = "Missing component '<component>'";
   public static final String ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE = "Invalid component '<component>': <failure>";
   public static final String ARGUMENT_PARSE_FAILURE_EITHER = "Could not resolve <primary> or <fallback> from '<input>'";
   public static final String EXCEPTION_UNEXPECTED = "An internal error occurred while attempting to perform this command.";
   public static final String EXCEPTION_INVALID_ARGUMENT = "Invalid command argument: <cause>.";
   public static final String EXCEPTION_NO_SUCH_COMMAND = "Unknown command.";
   public static final String EXCEPTION_NO_PERMISSION = "I'm sorry, but you do not have permission to perform this command.";
   public static final String EXCEPTION_INVALID_SENDER = "<actual> is not allowed to execute that command. Must be of type <expected>";
   public static final String EXCEPTION_INVALID_SENDER_LIST = "<actual> is not allowed to execute that command. Must be one of <expected>";
   public static final String EXCEPTION_INVALID_SYNTAX = "Invalid command syntax. Correct command syntax is: <syntax>.";
   private static final CaptionProvider<?> PROVIDER = CaptionProvider.constantProvider()
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_BOOLEAN, "Could not parse boolean from '<input>'")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER, "'<input>' is not a valid number in the range <min> to <max>")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_CHAR, "'<input>' is not a valid character")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_ENUM, "'<input>' is not one of the following: <acceptableValues>")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_STRING, "'<input>' is not a valid string of type <stringMode>")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_UUID, "'<input>' is not a valid UUID")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_REGEX, "'<input>' does not match '<pattern>'")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_COLOR, "'<input>' is not a valid color")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION, "'<input>' is not a duration format")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG, "Unknown flag '<flag>'")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG, "Duplicate flag '<flag>'")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED, "No flag started. Don't know what to do with '<input>'")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT, "Missing argument for '<flag>'")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION, "You don't have permission to use '<flag>'")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT, "Missing component '<component>'")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE, "Invalid component '<component>': <failure>")
      .putCaption(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_EITHER, "Could not resolve <primary> or <fallback> from '<input>'")
      .putCaption(StandardCaptionKeys.EXCEPTION_UNEXPECTED, "An internal error occurred while attempting to perform this command.")
      .putCaption(StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT, "Invalid command argument: <cause>.")
      .putCaption(StandardCaptionKeys.EXCEPTION_NO_SUCH_COMMAND, "Unknown command.")
      .putCaption(StandardCaptionKeys.EXCEPTION_NO_PERMISSION, "I'm sorry, but you do not have permission to perform this command.")
      .putCaption(StandardCaptionKeys.EXCEPTION_INVALID_SENDER, "<actual> is not allowed to execute that command. Must be of type <expected>")
      .putCaption(StandardCaptionKeys.EXCEPTION_INVALID_SENDER_LIST, "<actual> is not allowed to execute that command. Must be one of <expected>")
      .putCaption(StandardCaptionKeys.EXCEPTION_INVALID_SYNTAX, "Invalid command syntax. Correct command syntax is: <syntax>.")
      .build();

   @Override
   public @NonNull CaptionProvider<C> delegate() {
      return (CaptionProvider<C>)PROVIDER;
   }
}
