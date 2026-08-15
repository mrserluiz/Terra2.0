package com.dfsek.terra.api.util.vector;

import com.dfsek.terra.api.util.MathUtil;
import org.jetbrains.annotations.NotNull;

public class Vector3 {
   private static final Vector3 ZERO = new Vector3(0.0, 0.0, 0.0);
   private static final Vector3 UNIT = new Vector3(0.0, 1.0, 0.0);
   protected double x;
   protected double y;
   protected double z;

   private Vector3(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static Vector3 zero() {
      return ZERO;
   }

   public static Vector3 unit() {
      return UNIT;
   }

   public static Vector3 of(double x, double y, double z) {
      return new Vector3(x, y, z);
   }

   public double lengthSquared() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public double length() {
      return Math.sqrt(this.lengthSquared());
   }

   public double inverseLength() {
      return MathUtil.invSqrt(this.lengthSquared());
   }

   public double distance(@NotNull Vector3 o) {
      return Math.sqrt(Math.pow(this.x - o.getX(), 2.0) + Math.pow(this.y - o.getY(), 2.0) + Math.pow(this.z - o.getZ(), 2.0));
   }

   public double distanceSquared(@NotNull Vector3 o) {
      return Math.pow(this.x - o.getX(), 2.0) + Math.pow(this.y - o.getY(), 2.0) + Math.pow(this.z - o.getZ(), 2.0);
   }

   public double dot(@NotNull Vector3 other) {
      return this.x * other.getX() + this.y * other.getY() + this.z * other.getZ();
   }

   public double getZ() {
      return this.z;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public int getBlockX() {
      return (int)Math.floor(this.x);
   }

   public int getBlockY() {
      return (int)Math.floor(this.y);
   }

   public int getBlockZ() {
      return (int)Math.floor(this.z);
   }

   public boolean isNormalized() {
      return MathUtil.equals(this.lengthSquared(), 1.0);
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 79 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
      hash = 79 * hash + (int)(Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
      return 79 * hash + (int)(Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Vector3 other)
         ? false
         : MathUtil.equals(this.x, other.getX()) && MathUtil.equals(this.y, other.getY()) && MathUtil.equals(this.z, other.getZ());
   }

   public Vector3Int toInt() {
      return Vector3Int.of(this.getBlockX(), this.getBlockY(), this.getBlockZ());
   }

   public Vector3.Mutable mutable() {
      return new Vector3.Mutable(this.x, this.y, this.z);
   }

   @Override
   public String toString() {
      return "(" + this.getX() + ", " + this.getY() + ", " + this.getZ() + ")";
   }

   public static class Mutable extends Vector3 {
      private Mutable(double x, double y, double z) {
         super(x, y, z);
      }

      public static Vector3.Mutable of(double x, double y, double z) {
         return new Vector3.Mutable(x, y, z);
      }

      public Vector3 immutable() {
         return Vector3.of(this.x, this.y, this.z);
      }

      @Override
      public double getZ() {
         return this.z;
      }

      public Vector3.Mutable setZ(double z) {
         this.z = z;
         return this;
      }

      @Override
      public double getX() {
         return this.x;
      }

      public Vector3.Mutable setX(double x) {
         this.x = x;
         return this;
      }

      @Override
      public double getY() {
         return this.y;
      }

      public Vector3.Mutable setY(double y) {
         this.y = y;
         return this;
      }

      @Override
      public double lengthSquared() {
         return this.x * this.x + this.y * this.y + this.z * this.z;
      }

      @Override
      public double length() {
         return Math.sqrt(this.lengthSquared());
      }

      @Override
      public double inverseLength() {
         return MathUtil.invSqrt(this.lengthSquared());
      }

      public Vector3.Mutable normalize() {
         return this.multiply(this.inverseLength());
      }

      public Vector3.Mutable subtract(int x, int y, int z) {
         this.x -= x;
         this.y -= y;
         this.z -= z;
         return this;
      }

      @Override
      public double dot(@NotNull Vector3 other) {
         return this.x * other.getX() + this.y * other.getY() + this.z * other.getZ();
      }

      public Vector3.Mutable subtract(Vector3 end) {
         this.x = this.x - end.getX();
         this.y = this.y - end.getY();
         this.z = this.z - end.getZ();
         return this;
      }

      public Vector3.Mutable multiply(double m) {
         this.x *= m;
         this.y *= m;
         this.z *= m;
         return this;
      }

      public Vector3.Mutable add(double x, double y, double z) {
         this.x += x;
         this.y += y;
         this.z += z;
         return this;
      }

      public Vector3.Mutable add(Vector3 other) {
         this.x = this.x + other.getX();
         this.y = this.y + other.getY();
         this.z = this.z + other.getZ();
         return this;
      }

      public Vector3.Mutable add(Vector3Int other) {
         this.x = this.x + other.getX();
         this.y = this.y + other.getY();
         this.z = this.z + other.getZ();
         return this;
      }

      public Vector3.Mutable add(Vector2 other) {
         this.x = this.x + other.getX();
         this.z = this.z + other.getZ();
         return this;
      }

      @NotNull
      public Vector3.Mutable rotateAroundAxis(@NotNull Vector3 axis, double angle) throws IllegalArgumentException {
         return this.rotateAroundNonUnitAxis(axis.isNormalized() ? axis : axis.mutable().normalize().immutable(), angle);
      }

      @NotNull
      public Vector3.Mutable rotateAroundNonUnitAxis(@NotNull Vector3 axis, double angle) throws IllegalArgumentException {
         double x = this.getX();
         double y = this.getY();
         double z = this.getZ();
         double x2 = axis.getX();
         double y2 = axis.getY();
         double z2 = axis.getZ();
         double cosTheta = MathUtil.cos(angle);
         double sinTheta = MathUtil.sin(angle);
         double dotProduct = this.dot(axis);
         double xPrime = x2 * dotProduct * (1.0 - cosTheta) + x * cosTheta + (-z2 * y + y2 * z) * sinTheta;
         double yPrime = y2 * dotProduct * (1.0 - cosTheta) + y * cosTheta + (z2 * x - x2 * z) * sinTheta;
         double zPrime = z2 * dotProduct * (1.0 - cosTheta) + z * cosTheta + (-y2 * x + x2 * y) * sinTheta;
         return this.setX(xPrime).setY(yPrime).setZ(zPrime);
      }

      @NotNull
      public Vector3.Mutable rotateAroundX(double angle) {
         double angleCos = MathUtil.cos(angle);
         double angleSin = MathUtil.sin(angle);
         double y = angleCos * this.getY() - angleSin * this.getZ();
         double z = angleSin * this.getY() + angleCos * this.getZ();
         return this.setY(y).setZ(z);
      }

      @NotNull
      public Vector3.Mutable rotateAroundY(double angle) {
         double angleCos = MathUtil.cos(angle);
         double angleSin = MathUtil.sin(angle);
         double x = angleCos * this.getX() + angleSin * this.getZ();
         double z = -angleSin * this.getX() + angleCos * this.getZ();
         return this.setX(x).setZ(z);
      }

      @NotNull
      public Vector3.Mutable rotateAroundZ(double angle) {
         double angleCos = MathUtil.cos(angle);
         double angleSin = MathUtil.sin(angle);
         double x = angleCos * this.getX() - angleSin * this.getY();
         double y = angleSin * this.getX() + angleCos * this.getY();
         return this.setX(x).setY(y);
      }

      @Override
      public int hashCode() {
         int hash = 13;
         hash = 79 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
         hash = 79 * hash + (int)(Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
         return 79 * hash + (int)(Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
      }

      @Override
      public int getBlockX() {
         return (int)Math.floor(this.x);
      }

      @Override
      public int getBlockY() {
         return (int)Math.floor(this.y);
      }

      @Override
      public int getBlockZ() {
         return (int)Math.floor(this.z);
      }
   }
}
