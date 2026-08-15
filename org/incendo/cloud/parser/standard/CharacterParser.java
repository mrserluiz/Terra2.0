package org.incendo.cloud.parser.standard;

import java.util.Objects;
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

@API(status = Status.STABLE)
public final class CharacterParser<C> implements ArgumentParser<C, Character> {
   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, Character> characterParser() {
      return ParserDescriptor.of(new CharacterParser<>(), Character.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, Character> characterComponent() {
      return CommandComponent.<C, Character>builder().parser(characterParser());
   }

   @Override
   public @NonNull ArgumentParseResult<Character> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      return commandInput.peekString().length() != 1
         ? ArgumentParseResult.failure(new CharacterParser.CharParseException(commandInput.peekString(), commandContext))
         : ArgumentParseResult.success(commandInput.read());
   }

   @API(status = Status.STABLE)
   public static final class CharParseException extends ParserException {
      private final String input;

      public CharParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
         super(CharacterParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_CHAR, CaptionVariable.of("input", input));
         this.input = input;
      }

      public @NonNull String input() {
         return this.input;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            CharacterParser.CharParseException that = (CharacterParser.CharParseException)o;
            return this.input.equals(that.input);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.input);
      }
   }
}
