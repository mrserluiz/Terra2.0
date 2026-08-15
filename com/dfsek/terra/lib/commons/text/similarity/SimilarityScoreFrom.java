package com.dfsek.terra.lib.commons.text.similarity;

import com.dfsek.terra.lib.commons.lang3.Validate;

public class SimilarityScoreFrom<R> {
   private final SimilarityScore<R> similarityScore;
   private final CharSequence left;

   public SimilarityScoreFrom(SimilarityScore<R> similarityScore, CharSequence left) {
      Validate.isTrue(similarityScore != null, "The edit distance may not be null.");
      this.similarityScore = similarityScore;
      this.left = left;
   }

   public R apply(CharSequence right) {
      return this.similarityScore.apply(this.left, right);
   }

   public CharSequence getLeft() {
      return this.left;
   }

   public SimilarityScore<R> getSimilarityScore() {
      return this.similarityScore;
   }
}
