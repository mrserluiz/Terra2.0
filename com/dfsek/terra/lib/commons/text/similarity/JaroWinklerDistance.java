package com.dfsek.terra.lib.commons.text.similarity;

public class JaroWinklerDistance implements EditDistance<Double> {
   @Deprecated
   public static final int INDEX_NOT_FOUND = -1;

   @Deprecated
   protected static int[] matches(CharSequence first, CharSequence second) {
      return JaroWinklerSimilarity.matches(first, second);
   }

   public Double apply(CharSequence left, CharSequence right) {
      return this.apply(SimilarityInput.input(left), SimilarityInput.input(right));
   }

   public <E> Double apply(SimilarityInput<E> left, SimilarityInput<E> right) {
      if (left != null && right != null) {
         return 1.0 - JaroWinklerSimilarity.INSTANCE.apply(left, right);
      } else {
         throw new IllegalArgumentException("CharSequences must not be null");
      }
   }
}
