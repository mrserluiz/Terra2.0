package com.dfsek.terra.lib.commons.text.similarity;

public class HammingDistance implements EditDistance<Integer> {
   public Integer apply(CharSequence left, CharSequence right) {
      return this.apply(SimilarityInput.input(left), SimilarityInput.input(right));
   }

   public <E> Integer apply(SimilarityInput<E> left, SimilarityInput<E> right) {
      if (left != null && right != null) {
         if (left.length() != right.length()) {
            throw new IllegalArgumentException("SimilarityInput must have the same length");
         }

         int distance = 0;

         for (int i = 0; i < left.length(); i++) {
            if (!left.at(i).equals(right.at(i))) {
               distance++;
            }
         }

         return distance;
      } else {
         throw new IllegalArgumentException("SimilarityInput must not be null");
      }
   }
}
