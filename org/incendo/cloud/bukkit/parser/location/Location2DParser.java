package org.incendo.cloud.bukkit.parser.location;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public final class Location2DParser<C> implements ArgumentParser<C, Location2D>, BlockingSuggestionProvider.Strings<C> {
   private final LocationCoordinateParser<C> locationCoordinateParser = new LocationCoordinateParser<>();

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, Location2D> location2DParser() {
      return ParserDescriptor.of(new Location2DParser<>(), Location2D.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, Location2D> location2DComponent() {
      return CommandComponent.<C, Location2D>builder().parser(location2DParser());
   }

   @Override
   public ArgumentParseResult<Location2D> parse(final CommandContext<C> commandContext, final CommandInput commandInput) {
      if (commandInput.remainingTokens() < 2) {
         return ArgumentParseResult.failure(
            new LocationParser.LocationParseException(
               commandContext, LocationParser.LocationParseException.FailureReason.WRONG_FORMAT, commandInput.remainingInput()
            )
         );
      }

      LocationCoordinate[] coordinates = new LocationCoordinate[2];

      for (int i = 0; i < 2; i++) {
         if (commandInput.peekString().isEmpty()) {
            return ArgumentParseResult.failure(
               new LocationParser.LocationParseException(
                  commandContext, LocationParser.LocationParseException.FailureReason.WRONG_FORMAT, commandInput.remainingInput()
               )
            );
         }

         ArgumentParseResult<LocationCoordinate> coordinate = this.locationCoordinateParser.parse(commandContext, commandInput);
         if (coordinate.failure().isPresent()) {
            return ArgumentParseResult.failure(coordinate.failure().get());
         }

         coordinates[i] = coordinate.parsedValue().orElseThrow(NullPointerException::new);
      }

      CommandSender bukkitSender = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
      Location originalLocation;
      if (bukkitSender instanceof BlockCommandSender) {
         originalLocation = ((BlockCommandSender)bukkitSender).getBlock().getLocation();
      } else if (bukkitSender instanceof Entity) {
         originalLocation = ((Entity)bukkitSender).getLocation();
      } else {
         originalLocation = new Location((World)Bukkit.getWorlds().get(0), 0.0, 0.0, 0.0);
      }

      if (coordinates[0].type() == LocationCoordinateType.LOCAL && coordinates[1].type() != LocationCoordinateType.LOCAL) {
         return ArgumentParseResult.failure(
            new LocationParser.LocationParseException(commandContext, LocationParser.LocationParseException.FailureReason.MIXED_LOCAL_ABSOLUTE, "")
         );
      }

      if (coordinates[0].type() == LocationCoordinateType.ABSOLUTE) {
         originalLocation.setX(coordinates[0].coordinate());
      } else if (coordinates[0].type() == LocationCoordinateType.RELATIVE) {
         originalLocation.add(coordinates[0].coordinate(), 0.0, 0.0);
      }

      if (coordinates[1].type() == LocationCoordinateType.ABSOLUTE) {
         originalLocation.setZ(coordinates[1].coordinate());
      } else {
         if (coordinates[1].type() != LocationCoordinateType.RELATIVE) {
            Vector declaredPos = new Vector(coordinates[0].coordinate(), 0.0, coordinates[1].coordinate());
            Location local = LocationParser.toLocalSpace(originalLocation, declaredPos);
            return ArgumentParseResult.success(Location2D.from(originalLocation.getWorld(), local.getX(), local.getZ()));
         }

         originalLocation.add(0.0, 0.0, coordinates[1].coordinate());
      }

      return ArgumentParseResult.success(Location2D.from(originalLocation.getWorld(), originalLocation.getX(), originalLocation.getZ()));
   }

   @Override
   public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput input) {
      return LocationParser.getSuggestions(2, commandContext, input);
   }
}
