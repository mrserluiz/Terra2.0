package com.dfsek.terra.lib.commons.text.similarity;

public class LongestCommonSubsequenceDistance implements EditDistance<Integer> {
   public Integer apply(CharSequence left, CharSequence right) {
      if (left != null && right != null) {
         return left.length() + right.length() - 2 * LongestCommonSubsequence.INSTANCE.apply(left, right);
      } else {
         throw new IllegalArgumentException("Inputs must not be null");
      }
   }
}
