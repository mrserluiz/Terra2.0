package org.incendo.cloud.parser.standard;

import java.util.Objects;
import java.util.UUID;
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
public final class UUIDParser<C> implements ArgumentParser<C, UUID> {
   @API(status = Status.STABLE)
   public static <C> @NonNull ParserDescriptor<C, UUID> uuidParser() {
      return ParserDescriptor.of(new UUIDParser<>(), UUID.class);
   }

   @API(status = Status.STABLE)
   public static <C> CommandComponent.@NonNull Builder<C, UUID> uuidComponent() {
      return CommandComponent.<C, UUID>builder().parser(uuidParser());
   }

   @Override
   public @NonNull ArgumentParseResult<UUID> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String input = commandInput.readString();

      try {
         UUID uuid = UUID.fromString(input);
         return ArgumentParseResult.success(uuid);
      } catch (IllegalArgumentException e) {
         return ArgumentParseResult.failure(new UUIDParser.UUIDParseException(input, commandContext));
      }
   }

   @API(status = Status.STABLE)
   public static final class UUIDParseException extends ParserException {
      private final String input;

      public UUIDParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
         super(UUIDParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_UUID, CaptionVariable.of("input", input));
         this.input = input;
      }

      public String input() {
         return this.input;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            UUIDParser.UUIDParseException that = (UUIDParser.UUIDParseException)o;
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
