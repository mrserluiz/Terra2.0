package com.dfsek.terra.lib.commons.text.similarity;

import java.util.Arrays;
import java.util.Objects;

public class JaroWinklerSimilarity implements SimilarityScore<Double> {
   static final JaroWinklerSimilarity INSTANCE = new JaroWinklerSimilarity();

   protected static int[] matches(CharSequence first, CharSequence second) {
      return matches(SimilarityInput.input(first), SimilarityInput.input(second));
   }

   protected static <E> int[] matches(SimilarityInput<E> first, SimilarityInput<E> second) {
      SimilarityInput<E> max;
      SimilarityInput<E> min;
      if (first.length() > second.length()) {
         max = first;
         min = second;
      } else {
         max = second;
         min = first;
      }

      int range = Math.max(max.length() / 2 - 1, 0);
      int[] matchIndexes = new int[min.length()];
      Arrays.fill(matchIndexes, -1);
      boolean[] matchFlags = new boolean[max.length()];
      int matches = 0;

      for (int mi = 0; mi < min.length(); mi++) {
         E c1 = min.at(mi);
         int xi = Math.max(mi - range, 0);

         for (int xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
            if (!matchFlags[xi] && c1.equals(max.at(xi))) {
               matchIndexes[mi] = xi;
               matchFlags[xi] = true;
               matches++;
               break;
            }
         }
      }

      Object[] ms1 = new Object[matches];
      Object[] ms2 = new Object[matches];
      int i = 0;
      int si = 0;

      while (i < min.length()) {
         if (matchIndexes[i] != -1) {
            ms1[si] = min.at(i);
            si++;
         }

         i++;
      }

      i = 0;
      si = 0;

      while (i < max.length()) {
         if (matchFlags[i]) {
            ms2[si] = max.at(i);
            si++;
         }

         i++;
      }

      i = 0;

      for (int mi = 0; mi < ms1.length; mi++) {
         if (!ms1[mi].equals(ms2[mi])) {
            i++;
         }
      }

      si = 0;

      for (int mi = 0; mi < Math.min(4, min.length()) && first.at(mi).equals(second.at(mi)); mi++) {
         si++;
      }

      return new int[]{matches, i, si};
   }

   public Double apply(CharSequence left, CharSequence right) {
      return this.apply(SimilarityInput.input(left), SimilarityInput.input(right));
   }

   public <E> Double apply(SimilarityInput<E> left, SimilarityInput<E> right) {
      double defaultScalingFactor = 0.1;
      if (left == null || right == null) {
         throw new IllegalArgumentException("CharSequences must not be null");
      }

      if (Objects.equals(left, right)) {
         return 1.0;
      }

      int[] mtp = matches(left, right);
      double m = mtp[0];
      if (m == 0.0) {
         return 0.0;
      }

      double j = (m / left.length() + m / right.length() + (m - mtp[1] / 2.0) / m) / 3.0;
      return j < 0.7 ? j : j + 0.1 * mtp[2] * (1.0 - j);
   }
}
