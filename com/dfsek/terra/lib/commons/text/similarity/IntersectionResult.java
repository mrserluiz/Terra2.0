package com.dfsek.terra.lib.commons.text.similarity;

import java.util.Objects;

public class IntersectionResult {
   private final int sizeA;
   private final int sizeB;
   private final int intersection;

   public IntersectionResult(int sizeA, int sizeB, int intersection) {
      if (sizeA < 0) {
         throw new IllegalArgumentException("Set size |A| is not positive: " + sizeA);
      }

      if (sizeB < 0) {
         throw new IllegalArgumentException("Set size |B| is not positive: " + sizeB);
      }

      if (intersection >= 0 && intersection <= Math.min(sizeA, sizeB)) {
         this.sizeA = sizeA;
         this.sizeB = sizeB;
         this.intersection = intersection;
      } else {
         throw new IllegalArgumentException("Invalid intersection of A and B: " + intersection);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         IntersectionResult result = (IntersectionResult)o;
         return this.sizeA == result.sizeA && this.sizeB == result.sizeB && this.intersection == result.intersection;
      } else {
         return false;
      }
   }

   public int getIntersection() {
      return this.intersection;
   }

   public int getSizeA() {
      return this.sizeA;
   }

   public int getSizeB() {
      return this.sizeB;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.sizeA, this.sizeB, this.intersection);
   }

   @Override
   public String toString() {
      return "Size A: " + this.sizeA + ", Size B: " + this.sizeB + ", Intersection: " + this.intersection;
   }
}
