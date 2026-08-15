package com.dfsek.terra.api.util;

public enum Rotation {
   CW_90(90),
   CW_180(180),
   CCW_90(270),
   NONE(0);

   private final int degrees;

   Rotation(int degrees) {
      this.degrees = degrees;
   }

   public static Rotation fromDegrees(int deg) {
      return switch (Math.floorMod(deg, 360)) {
         case 0 -> NONE;
         case 90 -> CW_90;
         case 180 -> CW_180;
         case 270 -> CCW_90;
         default -> throw new IllegalArgumentException();
      };
   }

   public Rotation inverse() {
      return switch (this) {
         case CW_90 -> CCW_90;
         case CW_180 -> CW_180;
         case CCW_90 -> CW_90;
         case NONE -> NONE;
      };
   }

   public Rotation rotate(Rotation rotation) {
      return fromDegrees(this.getDegrees() + rotation.getDegrees());
   }

   public int getDegrees() {
      return this.degrees;
   }

   public enum Axis {
      X,
      Y,
      Z;
   }
}
