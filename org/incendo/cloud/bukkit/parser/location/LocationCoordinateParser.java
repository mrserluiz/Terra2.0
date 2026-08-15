package org.incendo.cloud.bukkit.parser.location;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.DoubleParser;

public final class LocationCoordinateParser<C> implements ArgumentParser<C, LocationCoordinate> {
   @Override
   public @NonNull ArgumentParseResult<@NonNull LocationCoordinate> parse(
      final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput
   ) {
      String input = commandInput.skipWhitespace().peekString();
      LocationCoordinateType locationCoordinateType;
      if (commandInput.peek() == '^') {
         locationCoordinateType = LocationCoordinateType.LOCAL;
         commandInput.moveCursor(1);
      } else if (commandInput.peek() == '~') {
         locationCoordinateType = LocationCoordinateType.RELATIVE;
         commandInput.moveCursor(1);
      } else {
         locationCoordinateType = LocationCoordinateType.ABSOLUTE;
      }

      double coordinate;
      try {
         boolean empty = commandInput.peekString().isEmpty() || commandInput.peek() == ' ';
         coordinate = empty ? 0.0 : commandInput.readDouble();
         if (commandInput.hasRemainingInput()) {
            commandInput.skipWhitespace();
         }
      } catch (Exception e) {
         return ArgumentParseResult.failure(
            new DoubleParser.DoubleParseException(input, new DoubleParser(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), commandContext)
         );
      }

      return ArgumentParseResult.success(LocationCoordinate.of(locationCoordinateType, coordinate));
   }
}
