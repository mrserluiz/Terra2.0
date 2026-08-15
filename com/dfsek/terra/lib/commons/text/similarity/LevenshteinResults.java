package com.dfsek.terra.lib.commons.text.similarity;

import java.util.Objects;

public class LevenshteinResults {
   private final Integer distance;
   private final Integer insertCount;
   private final Integer deleteCount;
   private final Integer substituteCount;

   public LevenshteinResults(Integer distance, Integer insertCount, Integer deleteCount, Integer substituteCount) {
      this.distance = distance;
      this.insertCount = insertCount;
      this.deleteCount = deleteCount;
      this.substituteCount = substituteCount;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LevenshteinResults result = (LevenshteinResults)o;
         return Objects.equals(this.distance, result.distance)
            && Objects.equals(this.insertCount, result.insertCount)
            && Objects.equals(this.deleteCount, result.deleteCount)
            && Objects.equals(this.substituteCount, result.substituteCount);
      } else {
         return false;
      }
   }

   public Integer getDeleteCount() {
      return this.deleteCount;
   }

   public Integer getDistance() {
      return this.distance;
   }

   public Integer getInsertCount() {
      return this.insertCount;
   }

   public Integer getSubstituteCount() {
      return this.substituteCount;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.distance, this.insertCount, this.deleteCount, this.substituteCount);
   }

   @Override
   public String toString() {
      return "Distance: " + this.distance + ", Insert: " + this.insertCount + ", Delete: " + this.deleteCount + ", Substitute: " + this.substituteCount;
   }
}
