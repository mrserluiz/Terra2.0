package org.incendo.cloud.caption;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public final class StandardCaptionKeys {
   private static final Collection<Caption> RECOGNIZED_CAPTIONS = new LinkedList<>();
   public static final Caption ARGUMENT_PARSE_FAILURE_BOOLEAN = of("argument.parse.failure.boolean");
   public static final Caption ARGUMENT_PARSE_FAILURE_NUMBER = of("argument.parse.failure.number");
   public static final Caption ARGUMENT_PARSE_FAILURE_CHAR = of("argument.parse.failure.char");
   public static final Caption ARGUMENT_PARSE_FAILURE_STRING = of("argument.parse.failure.string");
   public static final Caption ARGUMENT_PARSE_FAILURE_UUID = of("argument.parse.failure.uuid");
   public static final Caption ARGUMENT_PARSE_FAILURE_ENUM = of("argument.parse.failure.enum");
   public static final Caption ARGUMENT_PARSE_FAILURE_REGEX = of("argument.parse.failure.regex");
   public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG = of("argument.parse.failure.flag.unknown");
   public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG = of("argument.parse.failure.flag.duplicate_flag");
   public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED = of("argument.parse.failure.flag.no_flag_started");
   public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT = of("argument.parse.failure.flag.missing_argument");
   public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION = of("argument.parse.failure.flag.no_permission");
   public static final Caption ARGUMENT_PARSE_FAILURE_COLOR = of("argument.parse.failure.color");
   public static final Caption ARGUMENT_PARSE_FAILURE_DURATION = of("argument.parse.failure.duration");
   public static final Caption ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT = of("argument.parse.failure.aggregate.missing");
   public static final Caption ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE = of("argument.parse.failure.aggregate.failure");
   public static final Caption ARGUMENT_PARSE_FAILURE_EITHER = of("argument.parse.failure.either");
   public static final Caption EXCEPTION_UNEXPECTED = of("exception.unexpected");
   public static final Caption EXCEPTION_INVALID_ARGUMENT = of("exception.invalid_argument");
   public static final Caption EXCEPTION_NO_SUCH_COMMAND = of("exception.no_such_command");
   public static final Caption EXCEPTION_NO_PERMISSION = of("exception.no_permission");
   public static final Caption EXCEPTION_INVALID_SENDER = of("exception.invalid_sender");
   public static final Caption EXCEPTION_INVALID_SENDER_LIST = of("exception.invalid_sender_list");
   public static final Caption EXCEPTION_INVALID_SYNTAX = of("exception.invalid_syntax");

   private StandardCaptionKeys() {
   }

   private static @NonNull Caption of(final @NonNull String key) {
      Caption caption = Caption.of(key);
      RECOGNIZED_CAPTIONS.add(caption);
      return caption;
   }

   public static @NonNull Collection<@NonNull Caption> standardCaptionKeys() {
      return Collections.unmodifiableCollection(RECOGNIZED_CAPTIONS);
   }
}
