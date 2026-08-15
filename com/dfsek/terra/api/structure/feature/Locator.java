package com.dfsek.terra.api.structure.feature;

import com.dfsek.terra.api.world.chunk.generation.util.Column;

public interface Locator {
   default Locator and(Locator that) {
      return column -> this.getSuitableCoordinates(column).and(that.getSuitableCoordinates(column));
   }

   default Locator or(Locator that) {
      return column -> this.getSuitableCoordinates(column).or(that.getSuitableCoordinates(column));
   }

   default Locator xor(Locator that) {
      return column -> this.getSuitableCoordinates(column).xor(that.getSuitableCoordinates(column));
   }

   BinaryColumn getSuitableCoordinates(Column<?> var1);
}
