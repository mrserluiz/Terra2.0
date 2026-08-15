package io.leangen.geantyref;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

class WildcardTypeImpl implements WildcardType {
   private final Type[] upperBounds;
   private final Type[] lowerBounds;

   WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
      if (upperBounds.length == 0) {
         throw new IllegalArgumentException("There must be at least one upper bound. For an unbound wildcard, the upper bound must be Object");
      }

      this.upperBounds = upperBounds;
      this.lowerBounds = lowerBounds;
   }

   @Override
   public Type[] getUpperBounds() {
      return (Type[])this.upperBounds.clone();
   }

   @Override
   public Type[] getLowerBounds() {
      return (Type[])this.lowerBounds.clone();
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof WildcardType)) {
         return false;
      }

      WildcardType that = (WildcardType)other;
      return Arrays.equals(this.getLowerBounds(), that.getLowerBounds()) && Arrays.equals(this.getUpperBounds(), that.getUpperBounds());
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.getLowerBounds()) ^ Arrays.hashCode(this.getUpperBounds());
   }

   @Override
   public String toString() {
      if (this.lowerBounds.length > 0) {
         return "? super " + GenericTypeReflector.getTypeName(this.lowerBounds[0]);
      } else {
         return this.upperBounds[0] == Object.class ? "?" : "? extends " + GenericTypeReflector.getTypeName(this.upperBounds[0]);
      }
   }
}
