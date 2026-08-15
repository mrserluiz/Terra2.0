package com.dfsek.terra.api.util.vector;

import com.dfsek.terra.api.util.Rotation;

public class Vector2Int {
   private static final Vector2Int ZERO = new Vector2Int(0, 0);
   private static final Vector2Int UNIT = new Vector2Int(0, 1);
   protected int x;
   protected int z;

   protected Vector2Int(int x, int z) {
      this.x = x;
      this.z = z;
   }

   public static Vector2Int zero() {
      return ZERO;
   }

   public static Vector2Int unit() {
      return UNIT;
   }

   public static Vector2Int of(int x, int z) {
      return new Vector2Int(x, z);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public Vector3Int toVector3(int y) {
      return new Vector3Int(this.x, y, this.z);
   }

   public Vector2Int.Mutable mutable() {
      return new Vector2Int.Mutable(this.x, this.z);
   }

   public Vector2Int rotate(Rotation rotation) {
      return switch (rotation) {
         case CW_90 -> of(this.z, -this.x);
         case CCW_90 -> of(-this.z, this.x);
         case CW_180 -> of(-this.x, -this.z);
         default -> this;
      };
   }

   @Override
   public int hashCode() {
      return 31 * this.x + this.z;
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Vector2Int that) ? false : this.x == that.x && this.z == that.z;
   }

   public static class Mutable extends Vector2Int {
      protected Mutable(int x, int z) {
         super(x, z);
      }

      @Override
      public int getZ() {
         return this.z;
      }

      public void setZ(int z) {
         this.z = z;
      }

      @Override
      public int getX() {
         return this.x;
      }

      public void setX(int x) {
         this.x = x;
      }

      public Vector2Int immutable() {
         return new Vector2Int(this.x, this.z);
      }
   }
}
