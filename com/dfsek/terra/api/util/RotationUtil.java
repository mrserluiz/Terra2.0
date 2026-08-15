package com.dfsek.terra.api.util;

import com.dfsek.terra.api.util.vector.Vector2;

public final class RotationUtil {
   public static Vector2 rotateVector(Vector2 orig, Rotation r) {
      Vector2.Mutable copy = orig.mutable();
      switch (r) {
         case CW_90:
            copy.setX(orig.getZ()).setZ(-orig.getX());
            break;
         case CCW_90:
            copy.setX(-orig.getZ()).setZ(orig.getX());
            break;
         case CW_180:
            copy.multiply(-1.0);
      }

      return copy.immutable();
   }
}
