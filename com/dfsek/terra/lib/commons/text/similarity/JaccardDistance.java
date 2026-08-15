package com.dfsek.terra.lib.commons.text.similarity;

public class JaccardDistance implements EditDistance<Double> {
   public Double apply(CharSequence left, CharSequence right) {
      return this.apply(SimilarityInput.input(left), SimilarityInput.input(right));
   }

   public <E> Double apply(SimilarityInput<E> left, SimilarityInput<E> right) {
      return 1.0 - JaccardSimilarity.INSTANCE.apply(left, right);
   }
}
