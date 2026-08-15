package com.dfsek.terra.lib.commons.text.similarity;

import java.util.Arrays;

public class LevenshteinDetailedDistance implements EditDistance<LevenshteinResults> {
   private static final LevenshteinDetailedDistance INSTANCE = new LevenshteinDetailedDistance();
   private final Integer threshold;

   private static <E> LevenshteinResults findDetailedResults(SimilarityInput<E> left, SimilarityInput<E> right, int[][] matrix, boolean swapped) {
      int delCount = 0;
      int addCount = 0;
      int subCount = 0;
      int rowIndex = right.length();
      int columnIndex = left.length();
      int dataAtLeft = 0;
      int dataAtTop = 0;
      int dataAtDiagonal = 0;
      int data = 0;
      boolean deleted = false;
      boolean added = false;

      while (rowIndex >= 0 && columnIndex >= 0) {
         if (columnIndex == 0) {
            dataAtLeft = -1;
         } else {
            dataAtLeft = matrix[rowIndex][columnIndex - 1];
         }

         if (rowIndex == 0) {
            dataAtTop = -1;
         } else {
            dataAtTop = matrix[rowIndex - 1][columnIndex];
         }

         if (rowIndex > 0 && columnIndex > 0) {
            dataAtDiagonal = matrix[rowIndex - 1][columnIndex - 1];
         } else {
            dataAtDiagonal = -1;
         }

         if (dataAtLeft == -1 && dataAtTop == -1 && dataAtDiagonal == -1) {
            break;
         }

         data = matrix[rowIndex][columnIndex];
         if (columnIndex > 0 && rowIndex > 0 && left.at(columnIndex - 1).equals(right.at(rowIndex - 1))) {
            columnIndex--;
            rowIndex--;
         } else {
            deleted = false;
            added = false;
            if ((data - 1 != dataAtLeft || data > dataAtDiagonal || data > dataAtTop) && (dataAtDiagonal != -1 || dataAtTop != -1)) {
               if (data - 1 == dataAtTop && data <= dataAtDiagonal && data <= dataAtLeft || dataAtDiagonal == -1 && dataAtLeft == -1) {
                  rowIndex--;
                  if (swapped) {
                     delCount++;
                     deleted = true;
                  } else {
                     addCount++;
                     added = true;
                  }
               }
            } else {
               columnIndex--;
               if (swapped) {
                  addCount++;
                  added = true;
               } else {
                  delCount++;
                  deleted = true;
               }
            }

            if (!added && !deleted) {
               subCount++;
               columnIndex--;
               rowIndex--;
            }
         }
      }

      return new LevenshteinResults(addCount + delCount + subCount, addCount, delCount, subCount);
   }

   public static LevenshteinDetailedDistance getDefaultInstance() {
      return INSTANCE;
   }

   private static <E> LevenshteinResults limitedCompare(SimilarityInput<E> left, SimilarityInput<E> right, int threshold) {
      if (left != null && right != null) {
         if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must not be negative");
         }

         int n = left.length();
         int m = right.length();
         if (n == 0) {
            return m <= threshold ? new LevenshteinResults(m, m, 0, 0) : new LevenshteinResults(-1, 0, 0, 0);
         }

         if (m == 0) {
            return n <= threshold ? new LevenshteinResults(n, 0, n, 0) : new LevenshteinResults(-1, 0, 0, 0);
         }

         boolean swapped = false;
         if (n > m) {
            SimilarityInput<E> tmp = left;
            left = right;
            right = tmp;
            n = m;
            m = right.length();
            swapped = true;
         }

         int[] p = new int[n + 1];
         int[] d = new int[n + 1];
         int[][] matrix = new int[m + 1][n + 1];
         int index = 0;

         while (index <= n) {
            matrix[0][index] = index++;
         }

         index = 0;

         while (index <= m) {
            matrix[index][0] = index++;
         }

         index = Math.min(n, threshold) + 1;
         int i = 0;

         while (i < index) {
            p[i] = i++;
         }

         Arrays.fill(p, index, p.length, Integer.MAX_VALUE);
         Arrays.fill(d, Integer.MAX_VALUE);

         for (int j = 1; j <= m; j++) {
            E rightJ = right.at(j - 1);
            d[0] = j;
            int min = Math.max(1, j - threshold);
            int max = j > Integer.MAX_VALUE - threshold ? n : Math.min(n, j + threshold);
            if (min > max) {
               return new LevenshteinResults(-1, 0, 0, 0);
            }

            if (min > 1) {
               d[min - 1] = Integer.MAX_VALUE;
            }

            for (int ix = min; ix <= max; ix++) {
               if (left.at(ix - 1).equals(rightJ)) {
                  d[ix] = p[ix - 1];
               } else {
                  d[ix] = 1 + Math.min(Math.min(d[ix - 1], p[ix]), p[ix - 1]);
               }

               matrix[j][ix] = d[ix];
            }

            int[] tempD = p;
            p = d;
            d = tempD;
         }

         return p[n] <= threshold ? findDetailedResults(left, right, matrix, swapped) : new LevenshteinResults(-1, 0, 0, 0);
      } else {
         throw new IllegalArgumentException("CharSequences must not be null");
      }
   }

   private static <E> LevenshteinResults unlimitedCompare(SimilarityInput<E> left, SimilarityInput<E> right) {
      if (left != null && right != null) {
         int n = left.length();
         int m = right.length();
         if (n == 0) {
            return new LevenshteinResults(m, m, 0, 0);
         }

         if (m == 0) {
            return new LevenshteinResults(n, 0, n, 0);
         }

         boolean swapped = false;
         if (n > m) {
            SimilarityInput<E> tmp = left;
            left = right;
            right = tmp;
            n = m;
            m = right.length();
            swapped = true;
         }

         int[] p = new int[n + 1];
         int[] d = new int[n + 1];
         int[][] matrix = new int[m + 1][n + 1];
         int index = 0;

         while (index <= n) {
            matrix[0][index] = index++;
         }

         index = 0;

         while (index <= m) {
            matrix[index][0] = index++;
         }

         index = 0;

         while (index <= n) {
            p[index] = index++;
         }

         for (int j = 1; j <= m; j++) {
            E rightJ = right.at(j - 1);
            d[0] = j;

            for (int var16 = 1; var16 <= n; var16++) {
               int cost = left.at(var16 - 1).equals(rightJ) ? 0 : 1;
               d[var16] = Math.min(Math.min(d[var16 - 1] + 1, p[var16] + 1), p[var16 - 1] + cost);
               matrix[j][var16] = d[var16];
            }

            int[] tempD = p;
            p = d;
            d = tempD;
         }

         return findDetailedResults(left, right, matrix, swapped);
      } else {
         throw new IllegalArgumentException("CharSequences must not be null");
      }
   }

   @Deprecated
   public LevenshteinDetailedDistance() {
      this(null);
   }

   public LevenshteinDetailedDistance(Integer threshold) {
      if (threshold != null && threshold < 0) {
         throw new IllegalArgumentException("Threshold must not be negative");
      }

      this.threshold = threshold;
   }

   public LevenshteinResults apply(CharSequence left, CharSequence right) {
      return this.apply(SimilarityInput.input(left), SimilarityInput.input(right));
   }

   public <E> LevenshteinResults apply(SimilarityInput<E> left, SimilarityInput<E> right) {
      return this.threshold != null ? limitedCompare(left, right, this.threshold) : unlimitedCompare(left, right);
   }

   public Integer getThreshold() {
      return this.threshold;
   }
}
