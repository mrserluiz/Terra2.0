package org.incendo.cloud.parser.standard;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;

@API(status = Status.STABLE)
public final class StringArrayParser<C> implements ArgumentParser<C, String[]> {
   private static final Pattern FLAG_PATTERN = Pattern.compile("(-[A-Za-z_\\-0-9])|(--[A-Za-z_\\-0-9]*)");
   private final boolean flagYielding;

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, String[]> stringArrayParser() {
      return ParserDescriptor.of(new StringArrayParser<>(), String[].class);
   }

   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, String[]> flagYieldingStringArrayParser() {
      return ParserDescriptor.of(new StringArrayParser<>(true), String[].class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, String[]> characterComponent() {
      return CommandComponent.<C, String[]>builder().parser(stringArrayParser());
   }

   public StringArrayParser() {
      this.flagYielding = false;
   }

   @API(status = Status.STABLE)
   public StringArrayParser(final boolean flagYielding) {
      this.flagYielding = flagYielding;
   }

   @Override
   public @NonNull ArgumentParseResult<String @NonNull []> parse(
      final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput
   ) {
      int size = commandInput.remainingTokens();
      if (this.flagYielding) {
         List<String> result = new LinkedList<>();

         for (int i = 0; i < size; i++) {
            String string = commandInput.peekString();
            if (string.isEmpty() || FLAG_PATTERN.matcher(string).matches()) {
               break;
            }

            result.add(commandInput.readString());
         }

         return ArgumentParseResult.success(result.toArray(new String[0]));
      } else {
         String[] result = new String[size];

         for (int i = 0; i < result.length; i++) {
            result[i] = commandInput.readString();
         }

         return ArgumentParseResult.success(result);
      }
   }
}
