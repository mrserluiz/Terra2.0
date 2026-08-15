package org.incendo.cloud.bukkit.parser.location;

import java.util.Locale;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class LocationCoordinate {
   private final LocationCoordinateType type;
   private final double coordinate;

   private LocationCoordinate(final @NonNull LocationCoordinateType type, final double coordinate) {
      this.type = type;
      this.coordinate = coordinate;
   }

   public static @NonNull LocationCoordinate of(final @NonNull LocationCoordinateType type, final double coordinate) {
      return new LocationCoordinate(type, coordinate);
   }

   public @NonNull LocationCoordinateType type() {
      return this.type;
   }

   public double coordinate() {
      return this.coordinate;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LocationCoordinate that = (LocationCoordinate)o;
         return Double.compare(that.coordinate, this.coordinate) == 0 && this.type == that.type;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.coordinate);
   }

   @Override
   public String toString() {
      return String.format("LocationCoordinate{type=%s, coordinate=%f}", this.type.name().toLowerCase(Locale.ROOT), this.coordinate);
   }
}
