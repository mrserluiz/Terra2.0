package com.dfsek.terra.api.util.vector;

import com.dfsek.terra.api.util.MathUtil;

public class Vector2 {
   private static final Vector2 ZERO = new Vector2(0.0, 0.0);
   private static final Vector2 UNIT = new Vector2(0.0, 1.0);
   protected double x;
   protected double z;

   private Vector2(double x, double z) {
      this.x = x;
      this.z = z;
   }

   public static Vector2 zero() {
      return ZERO;
   }

   public static Vector2 unit() {
      return UNIT;
   }

   public static Vector2 of(double x, double z) {
      return new Vector2(x, z);
   }

   public double length() {
      return Math.sqrt(this.lengthSquared());
   }

   public double lengthSquared() {
      return this.x * this.x + this.z * this.z;
   }

   public double distance(Vector2 other) {
      return Math.sqrt(this.distanceSquared(other));
   }

   public double distanceSquared(Vector2 other) {
      double dx = other.getX() - this.x;
      double dz = other.getZ() - this.z;
      return dx * dx + dz * dz;
   }

   public Vector3 extrude(double y) {
      return Vector3.of(this.x, y, this.z);
   }

   public double getX() {
      return this.x;
   }

   public double getZ() {
      return this.z;
   }

   public int getBlockX() {
      return (int)Math.floor(this.x);
   }

   public int getBlockZ() {
      return (int)Math.floor(this.z);
   }

   @Override
   public int hashCode() {
      int hash = 17;
      hash = 31 * hash + Double.hashCode(this.x);
      return 31 * hash + Double.hashCode(this.z);
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Vector2 other) ? false : MathUtil.equals(this.x, other.x) && MathUtil.equals(this.z, other.z);
   }

   public Vector2.Mutable mutable() {
      return new Vector2.Mutable(this.x, this.z);
   }

   @Override
   public String toString() {
      return "(" + this.x + ", " + this.z + ")";
   }

   public static class Mutable extends Vector2 {
      private Mutable(double x, double z) {
         super(x, z);
      }

      @Override
      public double getX() {
         return this.x;
      }

      public Vector2.Mutable setX(double x) {
         this.x = x;
         return this;
      }

      @Override
      public double getZ() {
         return this.z;
      }

      public Vector2.Mutable setZ(double z) {
         this.z = z;
         return this;
      }

      public Vector2 immutable() {
         return Vector2.of(this.x, this.z);
      }

      @Override
      public double length() {
         return Math.sqrt(this.lengthSquared());
      }

      @Override
      public double lengthSquared() {
         return this.x * this.x + this.z * this.z;
      }

      public Vector2.Mutable add(double x, double z) {
         this.x += x;
         this.z += z;
         return this;
      }

      public Vector2.Mutable multiply(double m) {
         this.x *= m;
         this.z *= m;
         return this;
      }

      public Vector2.Mutable add(Vector2 other) {
         this.x = this.x + other.getX();
         this.z = this.z + other.getZ();
         return this;
      }

      public Vector2.Mutable subtract(Vector2 other) {
         this.x = this.x - other.getX();
         this.z = this.z - other.getZ();
         return this;
      }

      public Vector2.Mutable normalize() {
         this.divide(this.length());
         return this;
      }

      public Vector2.Mutable divide(double d) {
         this.x /= d;
         this.z /= d;
         return this;
      }
   }
}
