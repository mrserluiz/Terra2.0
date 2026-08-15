package com.dfsek.terra.lib.commons.text.similarity;

import java.util.HashSet;
import java.util.Set;

public class JaccardSimilarity implements SimilarityScore<Double> {
   static final JaccardSimilarity INSTANCE = new JaccardSimilarity();

   public Double apply(CharSequence left, CharSequence right) {
      return this.apply(SimilarityInput.input(left), SimilarityInput.input(right));
   }

   public <E> Double apply(SimilarityInput<E> left, SimilarityInput<E> right) {
      if (left != null && right != null) {
         int leftLength = left.length();
         int rightLength = right.length();
         if (leftLength == 0 && rightLength == 0) {
            return 1.0;
         }

         if (leftLength != 0 && rightLength != 0) {
            Set<E> leftSet = new HashSet<>();

            for (int i = 0; i < leftLength; i++) {
               leftSet.add(left.at(i));
            }

            Set<E> rightSet = new HashSet<>();

            for (int i = 0; i < rightLength; i++) {
               rightSet.add(right.at(i));
            }

            Set<E> unionSet = new HashSet<>(leftSet);
            unionSet.addAll(rightSet);
            int intersectionSize = leftSet.size() + rightSet.size() - unionSet.size();
            return 1.0 * intersectionSize / unionSet.size();
         } else {
            return 0.0;
         }
      } else {
         throw new IllegalArgumentException("Input cannot be null");
      }
   }
}
