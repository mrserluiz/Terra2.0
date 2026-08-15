package org.incendo.cloud.parser.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
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
public final class EnumParser<C, E extends Enum<E>> implements ArgumentParser<C, E>, BlockingSuggestionProvider.Strings<C> {
   private final Class<E> enumClass;
   private final EnumSet<E> acceptedValues;

   @API(status = Status.STABLE)
   public static <C, E extends Enum<E>> @NonNull ParserDescriptor<C, E> enumParser(final @NonNull Class<E> enumClass) {
      return ParserDescriptor.of(new EnumParser<>(enumClass), enumClass);
   }

   @API(status = Status.STABLE)
   public static <C, E extends Enum<E>> CommandComponent.@NonNull Builder<C, E> enumComponent(final @NonNull Class<E> enumClass) {
      return CommandComponent.<C, E>builder().parser(enumParser(enumClass));
   }

   public EnumParser(final @NonNull Class<E> enumClass) {
      this.enumClass = enumClass;
      this.acceptedValues = EnumSet.allOf(enumClass);
   }

   public @NonNull Class<E> enumClass() {
      return this.enumClass;
   }

   public @NonNull Collection<@NonNull E> acceptedValues() {
      return Collections.unmodifiableSet(this.acceptedValues);
   }

   @Override
   public @NonNull ArgumentParseResult<E> parse(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      String input = commandInput.readString();

      for (E value : this.acceptedValues) {
         if (value.name().equalsIgnoreCase(input)) {
            return ArgumentParseResult.success(value);
         }
      }

      return ArgumentParseResult.failure(new EnumParser.EnumParseException(input, this.enumClass, commandContext));
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return EnumSet.allOf(this.enumClass).stream().map(e -> e.name().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
   }

   @API(status = Status.STABLE)
   public static final class EnumParseException extends ParserException {
      private final String input;
      private final Class<? extends Enum<?>> enumClass;

      public EnumParseException(final @NonNull String input, final @NonNull Class<? extends Enum<?>> enumClass, final @NonNull CommandContext<?> context) {
         super(
            EnumParser.class,
            context,
            StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_ENUM,
            CaptionVariable.of("input", input),
            CaptionVariable.of("acceptableValues", join(enumClass))
         );
         this.input = input;
         this.enumClass = enumClass;
      }

      private static @NonNull String join(final @NonNull Class<? extends Enum> clazz) {
         EnumSet<?> enumSet = EnumSet.allOf((Class<E>)clazz);
         return enumSet.stream().map(e -> e.toString().toLowerCase(Locale.ROOT)).collect(Collectors.joining(", "));
      }

      public @NonNull String input() {
         return this.input;
      }

      public @NonNull Class<? extends Enum<?>> enumClass() {
         return this.enumClass;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            EnumParser.EnumParseException that = (EnumParser.EnumParseException)o;
            return this.input.equals(that.input) && this.enumClass.equals(that.enumClass);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.input, this.enumClass);
      }
   }
}
