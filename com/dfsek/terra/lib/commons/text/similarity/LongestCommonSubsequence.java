package com.dfsek.terra.lib.commons.text.similarity;

public class LongestCommonSubsequence implements SimilarityScore<Integer> {
   static final LongestCommonSubsequence INSTANCE = new LongestCommonSubsequence();

   private static int[] algorithmB(CharSequence left, CharSequence right) {
      int m = left.length();
      int n = right.length();
      int[][] dpRows = new int[2][1 + n];

      for (int i = 1; i <= m; i++) {
         int[] temp = dpRows[0];
         dpRows[0] = dpRows[1];
         dpRows[1] = temp;

         for (int j = 1; j <= n; j++) {
            if (left.charAt(i - 1) == right.charAt(j - 1)) {
               dpRows[1][j] = dpRows[0][j - 1] + 1;
            } else {
               dpRows[1][j] = Math.max(dpRows[1][j - 1], dpRows[0][j]);
            }
         }
      }

      return dpRows[1];
   }

   private static String algorithmC(CharSequence left, CharSequence right) {
      int m = left.length();
      int n = right.length();
      StringBuilder out = new StringBuilder();
      if (m == 1) {
         char leftCh = left.charAt(0);

         for (int j = 0; j < n; j++) {
            if (leftCh == right.charAt(j)) {
               out.append(leftCh);
               break;
            }
         }
      } else if (n > 0 && m > 1) {
         int mid = m / 2;
         CharSequence leftFirstPart = left.subSequence(0, mid);
         CharSequence leftSecondPart = left.subSequence(mid, m);
         int[] l1 = algorithmB(leftFirstPart, right);
         int[] l2 = algorithmB(reverse(leftSecondPart), reverse(right));
         int k = 0;
         int t = 0;

         for (int j = 0; j <= n; j++) {
            int s = l1[j] + l2[n - j];
            if (t < s) {
               t = s;
               k = j;
            }
         }

         out.append(algorithmC(leftFirstPart, right.subSequence(0, k)));
         out.append(algorithmC(leftSecondPart, right.subSequence(k, n)));
      }

      return out.toString();
   }

   private static String reverse(CharSequence s) {
      return new StringBuilder(s).reverse().toString();
   }

   public Integer apply(CharSequence left, CharSequence right) {
      if (left != null && right != null) {
         int leftSz = left.length();
         int rightSz = right.length();
         if (leftSz == 0 || rightSz == 0) {
            return 0;
         } else {
            return leftSz < rightSz ? algorithmB(right, left)[leftSz] : algorithmB(left, right)[rightSz];
         }
      } else {
         throw new IllegalArgumentException("Inputs must not be null");
      }
   }

   @Deprecated
   public CharSequence logestCommonSubsequence(CharSequence left, CharSequence right) {
      return this.longestCommonSubsequence(left, right);
   }

   public CharSequence longestCommonSubsequence(CharSequence left, CharSequence right) {
      if (left != null && right != null) {
         int leftSz = left.length();
         int rightSz = right.length();
         if (leftSz == 0 || rightSz == 0) {
            return "";
         } else {
            return leftSz < rightSz ? algorithmC(right, left) : algorithmC(left, right);
         }
      } else {
         throw new IllegalArgumentException("Inputs must not be null");
      }
   }

   @Deprecated
   public int[][] longestCommonSubstringLengthArray(CharSequence left, CharSequence right) {
      int[][] lcsLengthArray = new int[left.length() + 1][right.length() + 1];

      for (int i = 0; i < left.length(); i++) {
         for (int j = 0; j < right.length(); j++) {
            if (i == 0) {
               lcsLengthArray[i][j] = 0;
            }

            if (j == 0) {
               lcsLengthArray[i][j] = 0;
            }

            if (left.charAt(i) == right.charAt(j)) {
               lcsLengthArray[i + 1][j + 1] = lcsLengthArray[i][j] + 1;
            } else {
               lcsLengthArray[i + 1][j + 1] = Math.max(lcsLengthArray[i + 1][j], lcsLengthArray[i][j + 1]);
            }
         }
      }

      return lcsLengthArray;
   }
}
