package com.dfsek.terra.lib.commons.text.similarity;

import java.util.Objects;

final class SimilarityCharacterInput implements SimilarityInput<Character> {
   private final CharSequence cs;

   SimilarityCharacterInput(CharSequence cs) {
      if (cs == null) {
         throw new IllegalArgumentException("CharSequence");
      }

      this.cs = cs;
   }

   public Character at(int index) {
      return this.cs.charAt(index);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      SimilarityCharacterInput other = (SimilarityCharacterInput)obj;
      return Objects.equals(this.cs, other.cs);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.cs);
   }

   @Override
   public int length() {
      return this.cs.length();
   }

   @Override
   public String toString() {
      return this.cs.toString();
   }
}
