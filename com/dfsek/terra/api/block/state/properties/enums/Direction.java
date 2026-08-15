package com.dfsek.terra.api.block.state.properties.enums;

import com.dfsek.terra.api.util.Rotation;
import com.dfsek.terra.api.util.generic.Construct;

public enum Direction {
   NORTH(0, 0, 0, -1),
   EAST(1, 1, 0, 0),
   SOUTH(2, 0, 0, 1),
   WEST(3, -1, 0, 0),
   UP(-1, 0, 1, 0),
   DOWN(-1, 0, -1, 0);

   private static final Direction[] rotations = Construct.construct(() -> new Direction[]{NORTH, SOUTH, EAST, WEST});
   private final int rotation;
   private final int modX;
   private final int modY;
   private final int modZ;

   Direction(int rotation, int modX, int modY, int modZ) {
      this.rotation = rotation;
      this.modX = modX;
      this.modY = modY;
      this.modZ = modZ;
   }

   public Direction rotate(Rotation rotation) {
      return switch (this) {
         case UP, DOWN -> this;
         default -> rotations[(this.rotation + rotation.getDegrees() / 90) % 4];
      };
   }

   public Direction opposite() {
      return switch (this) {
         case NORTH -> SOUTH;
         case EAST -> WEST;
         case SOUTH -> NORTH;
         case WEST -> EAST;
         case UP -> DOWN;
         case DOWN -> UP;
      };
   }

   public int getModX() {
      return this.modX;
   }

   public int getModY() {
      return this.modY;
   }

   public int getModZ() {
      return this.modZ;
   }
}
